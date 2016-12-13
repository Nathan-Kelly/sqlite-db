/**
 * A mock client that will send the data it receives from the console and prints what
 * it receives from the server socket. Reads from the console in a thread so that it
 * can concurrently listen for data from user and data from the socket.
 *
 * Connects to the port number provided as an argument.
 */

import java.net.*;
import java.io.*;

public class TestClient {

	// number of required and optional for validity checking
	static final int minArgs = 1;
	static final int optArgs = 0;

	static final String usage = "Usage: TestClient <port>";

	static int port;

	/**
	 * Parses command line arguments to connect to a port on the localhost.
	 */
	public static void main(String args[]) {

		// check num arguments
		if (args.length < minArgs || args.length > minArgs + optArgs) {

			// invalid arguments case
			System.out.println(usage);
			return;
		}

		// parse args
		port = Integer.parseInt(args[0]);

		// declare resources
		try (Socket socket = new Socket("localhost", port);
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

			ConcurrentConsoleReader consoleReader = new ConcurrentConsoleReader(output);
			consoleReader.start();

			while (true) {
				// print any input from the socket
				String in = input.readLine();
				if (in != null) {
					System.out.println(in);
				} else {
					System.out.println("Server closed the socket.");
					consoleReader.stopSignal();
					return;
				}

				
			}
	
		} catch (UnknownHostException ex) {
			System.out.println("Localhost is an unknown host.");
		} catch (IOException ex) {
			System.out.println("An IO error occurred while creating the socket.");
		}
	}

	/**
	 * Allows waiting for input from the console while other processes occur.
	 */
	static class ConcurrentConsoleReader extends Thread {

		BufferedWriter out;
		volatile boolean stop;

		ConcurrentConsoleReader(BufferedWriter out) {
			this.out = out;
		}
		
		@Override
		public void run() {
			try (BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {

				while (!stop) {
					String input = consoleIn.readLine();

					// connection may have closed since starting to read
					if (!stop) {
						out.write(input, 0, input.length());
						out.newLine();
						out.flush();
					}
				}
			} catch (IOException ex) {
				System.out.println("IO Exception occurred when writing to socket.");
			} finally {
				try {
					out.close();
				} catch (IOException ex) {
					// closing down, doesn't matter
				}
			}
			
		}

		public void stopSignal() {
			this.stop = true;
		}
	}
}
