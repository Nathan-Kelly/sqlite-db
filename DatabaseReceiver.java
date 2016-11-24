import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.List;

class concurrentDBWriteThread implements Runnable{
    public ConcurrentLinkedQueue<String> clq;
    public boolean term = false;
    int timeout_cap = 10;
    int timeout_prog = timeout_cap;
    public concurrentDBWriteThread() {
	clq = new ConcurrentLinkedQueue<String>();	
    }
    
    public void run() {
	while(!term || !clq.isEmpty()) {
	    try { Thread.sleep(1); } catch (Exception e) {}
	    if(!clq.isEmpty()) {
		System.err.println("dumping...");
		timeout_prog--;
		String cs = clq.peek();
		String[] split = cs.split(" ");
		int er = 0;
		try {
		    
		    er = table_operations.insert_lux_entry(split[1], split[2], split[4], DatabaseReceiver.DEBUG);
		    if (DatabaseReceiver.DEBUG) System.out.print("Success: ");
		    if (DatabaseReceiver.DEBUG) System.out.println(cs + "\n");
		    clq.poll();
		}
		catch (Exception e) {
		    //System.err.println(e);
		    if(e.getMessage().equals("non-zero exit: 2")) //erase any malformed entries - can occur when something starts up
			clq.poll();
		    else
			System.err.println("Err: Database probably busy: " + e.getMessage()); 
			
		    //silently ignore - we can rety this later anyway
		    //check if shit was malformed or what
		}
		if(timeout_prog <= 0) {
		    timeout_prog = timeout_cap;
		    if(DatabaseReceiver.cmt.connected) {
			DatabaseReceiver.cmt.ready = true;
			while(DatabaseReceiver.cmt.done) {
			    try { Thread.sleep(1); } catch (Exception e) {}
			}
			DatabaseReceiver.cmt.done = false;
		    }
		}
	    }
	}
    }
}

class ConnectManThread implements Runnable {
    public boolean ready = false;
    public boolean done = false;
    static List<String> ls = null;
    public static boolean connected = false;
    
    public ConnectManThread() {           
    }

    public void run() {
	
	System.err.println("thread init");
	while(true) {
	    done = false;
	    try{
		System.err.println("loop start");
		run_command("iwgetid -r");
		for(String s : ls) {
		    if(s.equals("riot-waikato-072A")) {
			connected = true;
			System.out.println("Connected to server");
		    }
		    
		}
	    } catch(Exception e){
		connected = false;
		//e.printStackTrace();
	    }
	    
	    if(connected == false){
		try {
		    run_command("iwlist wlan1 scan");
		    
		    run_command("wpa_cli -i wlan1 remove_network 0");
		    run_command("wpa_cli -i wlan1 remove_network 1");
		    run_command("wpa_cli -i wlan1 remove_network 2");
		    run_command("wpa_cli -i wlan1 add_network");
		    run_command("wpa_cli -i wlan1 set_network 0 ssid \\\"riot-waikato-072A\\\"");
		    run_command("wpa_cli -i wlan1 set_network 0 psk \\\"riotwaikato\\\"");
		    run_command("wpa_cli -i wlan1 enable_network 0");
		} catch (Exception e) {
		    System.err.println(e);
		}
		
		try{
		    run_command("iwgetid -r");
		    for(String s : ls)
			if(s.equals("riot-waikato-072A")) {
			    System.err.println("Connected to server (2)");
			    connected = true;			
			}
		} catch(Exception e){
		    System.out.println("255");
		    //connected = false;
		}		
	    }
	    
	    System.out.println("Sleeping for 5s");
	    try {Thread.sleep(5000); } catch (Exception e) {}

	    if(connected == true && ready == true) {
		try {
		    Socket conn = new Socket("169.254.72.1", 65060);
		    try {
			PrintWriter out = new PrintWriter(conn.getOutputStream(), true);
			System.out.println("Connected");
			
			//run_command("sqlite3 tester2.db \\\" select \\* from lux;\\\"");
			//run_command("sqlite3 tester2.db \"PRAGMA journal_mode=WAL; select * from lux;\"");
			try {
			    run_command("sqlite3 tester2.db \"PRAGMA journal_mode=WAL; begin; select entry_date," +
					"_dev_id, lux from lux inner join entry on lux._seq = entry.seq; delete from lux; delete from entry; commit;\"");
			    for(String s : ls)  {
				System.out.println(s);
				out.println(s);
				out.flush();
			    }
			    
			    try {conn.close(); } catch (Exception e) {System.out.println("Couldn't close socket...");} //silently ignore
			} catch (Exception e) {  System.err.println("Could not read from database");  }			
		    } catch (Exception e) { System.err.println("Could not open printwriter: "); e.printStackTrace(); }
		} catch (Exception e) { System.err.println("Could not open socket connection"); }
		
		done = true;
		ready = false;
	    }
	}
    }
    
    public static int run_command(String cm) throws Exception {
	int exitStatus = run(arg_argv(cm));
	if(exitStatus != 0)
	    throw new Exception("non-zero exit: " + exitStatus);
	return exitStatus;
    }
							
    static String[] arg_argv(String arg) {
	return new String[] {"/bin/bash", "-c", arg};
    }

    public static void print_list(List<String> strl) {
	for(int i = 0; i < strl.size(); i++)
	    System.out.println(strl.get(i));
    }

    private static int run(String[] argv) throws Exception {
	Runtime rt = Runtime.getRuntime();
	Process proc = rt.exec(argv);
	String ln;
	BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	ls = new ArrayList<String>();
	
	while((ln = in.readLine()) != null)
	    ls.add(ln);
	return proc.waitFor();
    }
}


class DatabaseReceiver {
    public static concurrentDBWriteThread d;
    public static ConnectManThread cmt;
    public static Thread dbthread;
    public static Thread cmthread;
    public static boolean DEBUG = true;
    static boolean DIAGNOSTIC = false;
    static int maxTries = -1;
    static int tries = 0;

    static int last_packet_0x00        = -1;
    static int missed_packets_0x00     = 0;
    static int duplicated_packets_0x00 = 0;
    static int out_of_order_0x00       = 0;
    static int total_packets_0x00      = 0;    
    static ArrayList<String> seq_0x00  = new ArrayList<String>();

    static int last_packet_0x01        = -1;
    static int missed_packets_0x01     = 0;
    static int duplicated_packets_0x01 = 0;
    static int out_of_order_0x01       = 0;
    static int total_packets_0x01      = 0;    
    static ArrayList<String> seq_0x01  = new ArrayList<String>();
    
    public static void main(String argv[]) {
	if(argv.length > 0) {
	    try {
		maxTries = Integer.parseInt(argv[0]);
		DIAGNOSTIC = true;
		DEBUG = false;
	    }
	    catch (Exception e) {
		System.err.println("arg 0 <- Integer or null, thanks!");
	    }
	}
	try {
	    d = new concurrentDBWriteThread();
	    cmt = new ConnectManThread();
	    (dbthread = new Thread(d)).start();
	    (cmthread = new Thread(cmt)).start();
	    ServerSocket welcomeSocket = new ServerSocket(65051);
	    
	    System.out.println("Started listening for connections...");
	    while(maxTries == -1 || tries < maxTries) {	    
		Socket connectionSocket = welcomeSocket.accept();
		(new Thread(new DatabaseRecv_thread(connectionSocket))).start();
		tries++;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	try {
	    if(DIAGNOSTIC) try { Thread.sleep(1000); } catch (Exception e) {} //silently ignore thread timeout exception :^)
	} catch (Exception e) {//sleep silently 
	}
	if(DIAGNOSTIC) {
	    System.out.println();
	    System.out.println();

	    System.out.println("total tries/packets recieved: " + tries);
	    System.out.println("\nSummary for device 0x00:");
	    System.out.println("    last_packet:            " + last_packet_0x00);
	    System.out.println("    missed packets:         " + missed_packets_0x00);
	    System.out.println("    duplicate packets:      " + duplicated_packets_0x00);
	    System.out.println("    out of order packets:   " + out_of_order_0x00);
	    System.out.println("    total packets received: " + total_packets_0x00);

	    System.out.println();
	    System.out.println("\nSummary for device 0x01:");
	    System.out.println("    last_packet:            " + last_packet_0x01);
	    System.out.println("    missed packets:         " + missed_packets_0x01);
	    System.out.println("    duplicate packets:      " + duplicated_packets_0x01);
	    System.out.println("    out of order packets:   " + out_of_order_0x01);
	    System.out.println("    total packets received: " + total_packets_0x01);

	    d.term = true;
	}
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
	    if (DEBUG) System.err.print("_____RECV__________PORT:_");
	    if (DEBUG) System.err.println(connectionSocket.getPort() + "___");
	    try {
		BufferedReader inFromClient =
		    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		clientSentence = inFromClient.readLine();
	    }
	    catch (Exception e) {
		if (DEBUG) System.err.println("Could not create streamreader");
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
				    else {
					//Recieved first packet from devce 0x00
					if(!DEBUG)
					    System.err.println("Communication established with device " + dev_id);
					
				    }
				}
				last_packet_0x00 = k;
			    }
			    
			    //check if it's greater than the current greatest
			    //no, -> out of order
			}
			
		    }
		    else if(dev_id.equals("0x01")) {
			total_packets_0x01+=1;
			if(seq_0x01.contains(seqnum)) {
			    duplicated_packets_0x01++;
			    //duplicate packet
			}
			else {
			    seq_0x01.add(seqnum);
			    //TODO: parse seq num as int
			    int k = Integer.parseInt(seqnum);
			    if(k < last_packet_0x01)
				out_of_order_0x01++;
			    else {
				if (k -1 > last_packet_0x01) {
				    if(last_packet_0x01 != -1) {
					int dif = k - last_packet_0x01 - 1;
					missed_packets_0x01 += dif;
				    }
				    else {
					//Recieved first packet from devce 0x00
					if(!DEBUG)
					    System.err.println("Communication established with device " + dev_id);

				    }
				}
				last_packet_0x01 = k;
			    }
			    			    
			    //check if it's greater than the current greatest
			    //no, -> out of order
			}
		    }

		    d.clq.add(clientSentence);		    
		}
		catch (Exception e) {
		    if (DEBUG) System.err.println("Wrong Format: " + clientSentence);
		}
	    }
	    else {
		try {
		    table_operations.insert_motion_entry(split[1],
							 split[3] + " " +  split[4] + " " + split[5] + " " +
							 split[6] + " " +  split[7] + " " + split[8] + " " +
							 split[9] + " " +  split[10] + " " + split[11], 
							 split[2]);
		    if (DEBUG) System.out.print("Success: ");
		    if (DEBUG) System.out.println(clientSentence);
		}
		catch (Exception e) {
		    if (DEBUG) System.err.println("Wrong Format: " + clientSentence);
		}
	    }
	    try {
		connectionSocket.close();
	    } catch (Exception e) {} //silently ignore here - not important
	}
    }
}
