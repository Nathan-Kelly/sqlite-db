import java.io.*;
import java.net.*;

public class timeserver {
    public static void main(String[] args) {
	try {
	    ServerSocket ss = new ServerSocket(65053);
	    
	    for(;;) {
		try {
		    Socket con = ss.accept();
		    System.out.println("Connection from " + con.getPort());
		    (new Thread(new resThread(con))).start();
		}
		catch (Exception e) { //silently ignore exceptions
		    
		}
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static class resThread implements Runnable {
	Socket sock;
	public resThread(Socket sck) {
	    System.err.println("Thread made");
	    sock = sck;
	}
	public void run() {
	    try {
		System.err.println("Thread started");
		long cttime = System.currentTimeMillis()/1000L;
		new DataOutputStream(sock.getOutputStream()).writeBytes(cttime + "\n");
		System.err.println("Data Sent: " + cttime);
		sock.close();
		System.err.println("Socket Closed");
	    }
	    catch (IOException e) {
		System.err.println("Error Occured");
		e.printStackTrace();
	    }
	}
    }
}

