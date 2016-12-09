
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Uses a sliding window scheme to determine when sequence numbers are still
 * valid or can be safely discarded. This allows sequence numbers to overflow.
 *
 * Valid sequence numbers run from 0 to (distinctSeqNums - 1) inclusive.
 * Missing packets are recorded despite allowing sequence numbers to be removed
 * and reinserted, however if there is a long gap in sequence numbers
 * this will be very inaccurate.
 *
 * @author marianne
 */
public class WindowedSequenceList extends IntervalList {

    protected int distinctSeqNums = 256;
    protected int windowSize = 128;
    protected int windowLHS = 0;    // LHS of sliding window
    
    
    /* The missingCounter keeps track of gaps in sequence numbers once their
     * section of the number space is going to get discarded.  This happens when
     * the window is moved. */
    private int missingCounter = 0;

    /**
     * Get the position of the right-hand side of the window. Will wrap around
     * if it would be greater than the maximum allowed sequence number.
     *
     * @return
     */
    int getWindowRHS() {
        return (windowLHS + windowSize) % distinctSeqNums;
    }

    /**
     * Checks if the provided number is within the current window.
     *
     * @param number
     * @return
     */
    boolean isInWindow(int number) {

        // check validity
        if (number < distinctSeqNums && number >= 0) {
            
            // if window doesn't wrap around, number must be between window bounds
            if (windowLHS < getWindowRHS()) {
                return number >= windowLHS && number <= getWindowRHS();
            } else {
                
                // window wraps around
                return number >= windowLHS || number <= getWindowRHS();
            }
        } else {
            // number was outside of bounds
            return false;
        }
    }

    /**
     * Adds the given number to the list and moves the window to the right until
     * it is contained within it.
     * @param number
     * @return
     */
    @Override
    boolean add(int number) {
        // boundary checking
        if (number < 0 || number >= distinctSeqNums) {
            return false;
        }
        if (super.add(number)) {

            // if not in window, move window
            if (!isInWindow(number)) {
                if (!moveWindowToContain(number)) {
                    System.err.println("Serious error occurred when moving window.");
                    return false;
                }
            }
            
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves the window the minimum distance to the right needed to contain the
     * given number. Will wrap around if necessary.
     *
     * @param number
     * @return
     */
    boolean moveWindowToContain(int number) {
        // check number is in bounds
        if (number < distinctSeqNums && number >= 0) {
            
            int oldPosition = windowLHS;
            
            // adjust so RHS aligns with number
            windowLHS += number - getWindowRHS();
            windowLHS %= distinctSeqNums;
            
            if (windowLHS < 0) {
                windowLHS += distinctSeqNums;
            }
            
            // record any missing packets between sequence numbers now outside
            // window before they are removed
            if (windowLHS < oldPosition) {
                missingCounter += getNumMissingBetween(0, windowLHS);
                missingCounter += getNumMissingBetween(oldPosition, distinctSeqNums - 1);
            } else {
                missingCounter += getNumMissingBetween(oldPosition, windowLHS);
            }
            
            trimOutsideWindow();
            
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Removes sequence numbers that are not in the window from the list.
     */
    void trimOutsideWindow() {
        for (Iterator<IntervalList.Interval> it = list.iterator(); it.hasNext();) {
            IntervalList.Interval interval = it.next();
    
            // move start of interval to window LHS
            if (!isInWindow(interval.intervalStart) && windowLHS > interval.intervalStart) {
                interval.intervalStart = windowLHS;
            }
            
            // move end of interval to window RHS
            if (!isInWindow(interval.intervalEnd) && getWindowRHS() < interval.intervalEnd) {
                interval.intervalEnd = getWindowRHS();
            }
            
            // intervals should NOT wrap around like the window does so
            if (interval.intervalEnd < interval.intervalStart) {
                
                // remove the interval if it is no longer valid
                // this is the only safe way to remove while iterating
                it.remove();
            }
        }
    }
    
    /**
     * Get a string that describes the coverage of the window, eg. "0-128".
     * @return 
     */
    String getWindowCoverage() {
        return String.format("%d-%d", windowLHS, getWindowRHS());
    }
    
    /**
     * Gets the number of packets missing between two sequence numbers (inclusive)
     * of the numbers themselves if they are not present.  The first argument
     * must be less than the second argument and the interval does not wrap
     * around like a window does.
     * 
     * This is a complicated function with a lot of different cases.
     */
    int getNumMissingBetween(int num1, int num2) {

        int missing = num2 - num1;

        // bounds checking (num1)
        if (num1 < 0 || num1 >= distinctSeqNums) {
            return -1;
        }
        
        // bounds checking (num2)
        if (num2 < 0 || num2 >= distinctSeqNums) {
            return -1;
        }
        
        if (num2 < num1) {
            return -1;
        }
        
        Collections.sort(list);
        
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                
                // interval contains both numbers
                if (list.get(i).contains(num1) && list.get(i).contains(num2)) {
                    return 0;
                    // interval doesn't contain either number
                } else if (list.get(i).intervalStart > num1 && list.get(i).intervalEnd < num2) {
                    missing -= list.get(i).intervalEnd - list.get(i).intervalStart + 1;
                    
                    // interval contains num1
                } else if (list.get(i).contains(num1)) {
                    missing -= list.get(i).intervalEnd - num1 + 1;
                    
                    // interval contains num2
                } else if (list.get(i).contains(num2)) {
                    missing -= num2 - list.get(i).intervalStart + 1;
                    
                    // do not need to check any more Intervals
                    break;
                }
            }
            
            return missing;
        } else {
            // no sequence numbers received
            return num2 - num1;
        }
    }

    /**
     * Gets the total number of packets that have been dropped.  This includes
     * the counter, the number of sequence numbers missing from the start of the
     * window, and any packets between intervals.
     * @return 
     */
    @Override
    int getNumMissing() {
        
        int missing = 0;
        
        if (list.size() > 0) {
            missing += list.get(0).intervalStart - windowLHS - 1;
        }
        
        missing += super.getNumMissing();
        missing += missingCounter;
        return missing;
    }
    
    public static void main(String[] args) {
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        WindowedSequenceList list = new WindowedSequenceList();

        //accept input sequence numbers
        for (;;) {
            try {
                String in = cin.readLine();

                int seqNum = Integer.parseInt(in);

                if (list.add(seqNum)) {
                    System.out.println("Added sequence number: " + seqNum);
                } else {
                    System.out.println("Could not add sequence number: " + seqNum);
                }
                list.tidyList();
                
                System.out.println("Window: " + list.getWindowCoverage());

                System.out.println("SEQUENCE NUMBERS:");
                System.out.print(list);
                System.out.println("Missing packets: " + list.getNumMissing());

            } catch (IOException ex) {
                Logger.getLogger(IntervalList.class.getName()).log(Level.SEVERE, null, ex);
                return;
            } catch (NumberFormatException ex) {
                // couldn't parse number, try again
            }
        }
    }
}
