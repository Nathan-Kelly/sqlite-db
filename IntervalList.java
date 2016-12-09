
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores a list of intervals containing integers. Used to reduce the amount of
 * space needed for an array that will usually contain a lot of consecutive
 * numbers. Testing if the array contains a given integer will not be as
 * computationally expensive.
 * 
 * The intervals are inclusive of their starting and ending integers.
 * 
 * @author marianne
 */
public class IntervalList {

    ArrayList<IntervalList.Interval> list = new ArrayList<>();

    public class Interval implements Comparable<IntervalList.Interval> {

        int intervalStart;
        int intervalEnd;

        /**
         * Creates an interval one integer wide.
         * @param number 
         */
        Interval(int number) {
            intervalStart = number;
            intervalEnd = number;
        }

        /**
         * Creates an interval between first and last (inclusive).
         * @param first
         * @param last 
         */
        Interval(int first, int last) {
            intervalStart = first;
            intervalEnd = last;
        }

        /**
         * Check if this interval contains the number provided.
         *
         * @param number
         * @return
         */
        boolean contains(int number) {
            return number >= intervalStart && number <= intervalEnd;
        }

        /**
         * Checks if the provided number is adjacent to the interval. Adjacency
         * is defined as not in the interval and no integers are between the
         * number provided and the interval.
         *
         * @param number
         * @return True if adjacent.
         */
        boolean isAdjacentTo(int number) {
            if (intervalStart - number == 1 || number - intervalEnd == 1) {
                return true;
            }

            return false;
        }

        /**
         * Adds the number to this interval if possible.  Will fail if the
         * number is not adjacent to the interval.
         *
         * @param number
         * @return True if it was successful.
         */
        boolean addNumber(int number) {
            if (intervalStart - number == 1) {
                intervalStart = number;
                return true;
            } else if (number - intervalEnd == 1) {
                intervalEnd = number;
                return true;
            }

            return false;
        }

        @Override
        public String toString() {
            return String.format("%d-%d", intervalStart, intervalEnd);
        }

        /**
         * Sorts the intervals in ascending order of the starting number.
         *
         * @param interval
         * @return
         */
        @Override
        public int compareTo(IntervalList.Interval interval) {
            return this.intervalStart - interval.intervalStart;
        }
    }

    /**
     * Finds out if the provided number is within any interval in this list.
     *
     * @param number
     * @return
     */
    boolean contains(int number) {
        //start with most recently added (for sequence numbers this will be
        //faster
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).contains(number)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a number to this list. Will merge the number with an interval
     * already in the list if possible.
     *
     * @param number
     * @return
     */
    boolean add(int number) {
        //check if duplicate
        if (this.contains(number)) {
            return false;
        } else {
            //add to an existing interval, searching from end
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i).addNumber(number)) {
                    return true;
                }
            }

            //create a new interval
            list.add(new Interval(number));
            return true;
        }
    }

    /**
     * If the intervals at the given indexes are adjacent, combines them into a
     * single instance. Does not require indexes in any particular order.
     *
     * @param index1
     * @param index2
     * @return True if the intervals could be combined.
     */
    boolean combineIntervals(int index1, int index2) {
        IntervalList.Interval interval1 = list.get(index1);
        IntervalList.Interval interval2 = list.get(index2);
        IntervalList.Interval newInterval;

        //find how which interval comes first
        if (interval2.intervalStart - interval1.intervalEnd == 1) {
            newInterval = new IntervalList.Interval(interval1.intervalStart, interval2.intervalEnd);
        } else if (interval1.intervalStart - interval2.intervalEnd == 1) {
            newInterval = new IntervalList.Interval(interval2.intervalStart, interval1.intervalEnd);
        } else {
            //not adjacent
            return false;
        }

        //update list
        list.remove(interval1);
        list.remove(interval2);
        list.add(newInterval);

        return true;
    }

    @Override
    public String toString() {
        String str = "";

        for (int i = 0; i < list.size(); i++) {
            str += list.get(i).toString() + "\n";
        }

        return str;
    }

    /**
     * For testing purposes.
     *
     * @param argv
     */
    public static void main(String argv[]) {
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        IntervalList list = new IntervalList();

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

                System.out.print(list);
                System.out.println("Missing packets: " + list.getNumMissing());

            } catch (IOException ex) {
                Logger.getLogger(IntervalList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Attempts to combine all adjacent intervals.
     */
    void tidyList() {

        Collections.sort(list);

        //check all adjacent pairs from most recently added (so indexes don't
        //change)
        if (list.size() > 1) {
            for (int i = list.size() - 1; i > 0; i--) {
                combineIntervals(i, i - 1);
            }
        }
    }

    /**
     * Calculates the number of integers missing between the intervals in this
     * list.
     * 
     * @return 
     */
    int getNumMissing() {
        Collections.sort(list);

        int missing = 0;

        if (list.size() > 1) {
            for (int i = 0; i < list.size() - 1; i++) {
                //calculate number of integers between intervals
                missing += list.get(i + 1).intervalStart - list.get(i).intervalEnd - 1;
            }
        }
        
        return missing;
    }
}
