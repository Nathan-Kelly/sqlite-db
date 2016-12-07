
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maintains statistics about the clients that have connected to this AP.
 * Assumes that clients are uniquely identifiable by their device names.
 *
 * @author marianne
 */
public class ClientStatistics {

    ArrayList<ClientStatistics.Client> clientList = new ArrayList<>();

    int packetsReceived = 0;

    /**
     * Finds the index of the Client with the given device name, if it exists.
     *
     * @param name
     * @return Index of device or -1 if it could not be found.
     */
    public int getDeviceIndex(String name) {

        for (int i = 0; i < clientList.size(); i++) {

            //check device name matches
            if (clientList.get(i).deviceName.matches(name)) {
                return i;
            }
        }

        //no matches found
        return -1;
    }

    /**
     * Find the client device in the clientList array and update its statistics.
     *
     * @param deviceName
     */
    public void recordPacketStats(String deviceName, int seqNum) {

        packetsReceived++;

        int index = this.getDeviceIndex(deviceName);

        //add client if not in list
        if (index == -1) {
            //ensure client was added
            if (!this.addClient(new Client(deviceName))) {
                System.err.println("Could not find device '" + deviceName
                        + "' but could not add it to clientList.");
                return;
            } else {
                index = this.getDeviceIndex(deviceName);

                //ensure index is not -1
                if (index == -1) {
                    System.err.println("Could not find device '" + deviceName
                            + "' after it was added to list.");
                    return;
                }
            }
        }

        //record client statistics
        clientList.get(index).recordPacketStats(seqNum);

    }

    /**
     * Adds a client to the clientList if a device with that device name does
     * not already exist in the list.
     *
     * @param client
     * @return True if the client was added.
     */
    public boolean addClient(Client client) {
        //check if the device is in the array
        if (this.getDeviceIndex(client.deviceName) != -1) {
            return false;
        }

        clientList.add(client);
        return true;
    }

    /**
     * Prints summary statistics for all clients.
     *
     * @return
     */
    public void printStatistics() {
        System.out.println("Total packets received:\t" + packetsReceived);

        for (Client client : clientList) {
            client.printStatistics();
        }
    }

    /**
     * Holds statistical information about the communication with one client
     * device. Each client must have a unique device name.
     */
    public class Client {

        String deviceName;

        //device statistics
        int lastPacket = -1;
        int missedPackets = 0;
        int duplicatePackets = 0;
        int outOfOrderPackets = 0;
        int totalPackets = 0;
        IntervalList seqNums = new IntervalList();

        public Client(String deviceName) {
            this.deviceName = deviceName;
        }

        public boolean containsSeqNumber(int seqNum) {
            return seqNums.contains(seqNum);
        }

        public void recordPacketStats(int seqNum) {
            totalPackets++;

            //add sequence number
            if (!seqNums.add(seqNum)) {
                duplicatePackets++;
            }

            //check order
            if (seqNum < lastPacket) {
                outOfOrderPackets++;
            }

            lastPacket = seqNum;
        }

        /**
         * Prints summary statistics for this client device.s
         */
        public void printStatistics() {
            System.out.println("");
            System.out.println("Summary for device '" + deviceName + "':");
            System.out.println("\tlast packet:            " + lastPacket);
            System.out.println("\tmissed packets:         " + seqNums.getNumMissing());
            System.out.println("\tduplicate packets:      " + duplicatePackets);
            System.out.println("\tout of order packets:   " + outOfOrderPackets);
            System.out.println("\ttotal packets received: " + totalPackets);
        }
    }

    /**
     * For testing.
     *
     * @param argv
     */
    public static void main(String argv[]) {
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

        ClientStatistics stats = new ClientStatistics();

        for (;;) {
            try {
                //receive input in form "<device> <seqnum>" from user
                String packet = cin.readLine();

                String tokens[] = packet.split(" ");

                if (tokens.length == 2) {

                    stats.recordPacketStats(tokens[0], Integer.parseInt(tokens[1]));

                }

                stats.printStatistics();

            } catch (IOException ex) {
                Logger.getLogger(ClientStatistics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}