import java.io.*;
import java.net.*;
import java.util.ArrayList;

class DatabaseReceiver {
    static int maxTries = -1;
    static int tries = 0;

    static int last_packet_0x00 = -1;
    static int last_packet_0x01 = -1;

    static int missed_packets_0x00 = 0;
    static int duplicated_packets_0x00 = 0;
    static int out_of_order_0x00 = 0;

    static int total_packets_0x00 = 0;
    
    static ArrayList<String> seq_0x00 = new ArrayList<String>();
    static ArrayList<String> seq_0x01 = new ArrayList<String>();
    
    public static void main(String argv[]) throws Exception {
	if(argv.length > 0) {
	    try {
		maxTries = Integer.parseInt(argv[0]);
	    }
	    catch (Exception e) {
		System.err.println("arg 0 <- Integer or null, thanks!");
	    }
	}
	ServerSocket welcomeSocket = new ServerSocket(65051);
	
	while(maxTries == -1 || tries < maxTries) {	    
	    Socket connectionSocket = welcomeSocket.accept();
	    (new Thread(new DatabaseRecv_thread(connectionSocket))).start();
	    tries++;
	}

	System.out.println("total tries/packets recieved: " + tries);
	System.out.println("\nSummary for device 0x00:");
	System.out.println("    last_packet:          " + last_packet_0x00);
	System.out.println("    missed packets:       " + missed_packets_0x00);
	System.out.println("    duplicate packets:    " + duplicated_packets_0x00);
	System.out.println("    out of order packets: " + out_of_order_0x00);
    }

    static class Pair{
	public Pair(String A, String B) {
	    this.A = A;
	    this.B = B;
	}

	public String A;
	public String B;

	public boolean equals(Pair p) {
	    return (p.A.equals(A) && p.B.equals(B));
	}
    }

    static class DatabaseRecv_thread implements Runnable {
	private Socket connectionSocket;
	String clientSentence;
	public DatabaseRecv_thread(Socket skt) {
	    this.connectionSocket = skt;
	}
	
	public void run() {
	    System.err.print("_____RECV__________PORT:_");
	    System.err.println(connectionSocket.getPort() + "___");
	    try {
		BufferedReader inFromClient =
		    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		clientSentence = inFromClient.readLine();
	    }
	    catch (Exception e) {
		System.err.println("Could not create streamreader");
		return;
	    }
	    
	    String[] split = clientSentence.split(" ");
	    if(split[0].equals("lux")) {
		try {
		    //packet structure: LUX, ID, Val, Seq, Timestamp
		    //todo: use seq number for metrics
		    //todo: put this part in a thread, too
		    
		    //ID, VAL, Timestamp
		    String dev_id = split[1];
		    String val = split[2];
		    String Timestamp = split[4];
		    String seqnum = split[3];
		    if(dev_id.equals("0x00")) {
			total_packets_0x00+=1;
			if(seq_0x00.contains(seqnum)) {
			    duplicated_packets_0x00++;
			    //duplicate packet
			}
			else {
			    seq_0x00.add(seqnum);
			    //TODO: parse seq num as int
			    int k = Integer.parseInt(seqnum);
			    if(k < last_packet_0x00)
				out_of_order_0x00++;
			    else {
				if (k -1 > last_packet_0x00) {
				    if(last_packet_0x00 != -1) {
					int dif = k - last_packet_0x00 - 1;
					missed_packets_0x00 += dif;
				    }
				}
				last_packet_0x00 = k;
			    }
			    			    
			    //check if it's greater than the current greatest
			    //no, -> out of order
			}

		    }
		    table_operations.insert_lux_entry(split[1], split[2], split[4]);
		    System.out.print("Success: ");
		    System.out.println(clientSentence + "\n");
		}
		catch (Exception e) {
		    System.err.println("Wrong Format: " + clientSentence);
		}
	    }
	    else {
		try {
		    table_operations.insert_motion_entry(split[1],
							 split[3] + " " +  split[4] + " " + split[5] + " " +
							 split[6] + " " +  split[7] + " " + split[8] + " " +
							 split[9] + " " +  split[10] + " " + split[11], 
							 split[2]);
		    System.out.print("Success: ");
		    System.out.println(clientSentence);
		}
		catch (Exception e) {
		    System.err.println("Wrong Format: " + clientSentence);
		}
	    }
	    try {
		connectionSocket.close();
	    } catch (Exception e) {} //silently ignore here - not important	    
	}
    }
}
