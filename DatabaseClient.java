import java.io.*;
import java.util.*;
import java.net.*;

/*
Host a serverless chat session
*/
public class DatabaseClient
{
	public static void main(String[] args)
	{
		try
		{	

			// Get the input from the user
			BufferedReader myR = new BufferedReader(new InputStreamReader(System.in));
			
			// Get a connection to the address and socket
			InetAddress grp = InetAddress.getByName("10.1.1.1");
			MulticastSocket s = new MulticastSocket(61234);
	
			// Join the group			
			s.joinGroup(grp);

			// Generate a thread to listen for incoming messages
			ChatListen cl = new ChatListen(s, grp);
			cl.start();
			while(true)
			{
				// Send a message across
				//String msg = myR.readLine();
				//DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), grp, 40202);
				//s.send(hi);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

/*
Listens for incoming messages on the chat session
*/
class ChatListen extends Thread
{
	private MulticastSocket s;

	public ChatListen(MulticastSocket s, InetAddress grp)
	{
		this.s = s;
	}

	public void run()
	{
		while(true)
		{
			try
			{
				byte[] buf = new byte[1000];
				DatagramPacket recv = new DatagramPacket(buf, buf.length);
				s.receive(recv);
				InetAddress fromAddress = recv.getAddress();
				String str = new String(buf);

				String deviceId = str.substring(12, 23);
				String type = str.substring(0, 11);
				String time = str.substring(24, 35);
				String data = str.substring(36);
				
				table_operations.insert_motion_entry(deviceId, data, time);

				System.out.println(fromAddress.getHostAddress() + ": " + str);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
