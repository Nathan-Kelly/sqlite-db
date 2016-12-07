import java.io.*;
import java.net.*;

public class timeserver {
    
    static final int DEFAULT_PORT = 65053;
    static int port;
    
    public static void main(String[] args) {
        
        //use port number if provided
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else {
            port = DEFAULT_PORT;
        }
        
	try {
            //open socket
	    ServerSocket ss = new ServerSocket(port);
	    
	    for(;;) {
		try {
                    //start new thread on accepting a connection
		    Socket con = ss.accept();
		    System.out.println("Connection from " + con.getInetAddress()
                            + ":" + con.getPort());
		    (new Thread(new resThread(con))).start();
		} catch (IOException ex) {
                    System.err.println("Could not handle connection.");
                }
	    }
	} catch (IOException ex) {
            System.err.println("Could not open TimeServer socket.");
            ex.printStackTrace();
        }
    }

    /**
     * Handles a single connection from a client to the TimeServer.
     */
    static class resThread implements Runnable {
        
	Socket sock;
        
	public resThread(Socket sck) {
	    System.err.println("Thread made");
	    sock = sck;
	}
        
	public void run() {
	    try {
		System.err.println("Thread started");
                
                //send current time (in seconds)
		long cttime = System.currentTimeMillis()/1000L;
		new DataOutputStream(sock.getOutputStream()).writeBytes(cttime + "\n");
		System.err.println("Data Sent: " + cttime);
                
                //close connection
                System.err.println("Closing connection to " + sock.getInetAddress()
                    + ":" + sock.getPort());
		sock.close();
		System.err.println("Socket Closed");
	    } catch (IOException ex) {
                System.err.println("IOException occurred when timesyncing.");
            }
	}
    }
}
