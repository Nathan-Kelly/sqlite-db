import java.util.*;
import java.net.*;
import java.io.*;

public class HeadScanner
{
    static List<String> ls;
    
    public static void main(String[] args) throws Exception
    {
	boolean connected = false;
	
	while(true)
	{
	    try{
		run_command("iwgetid -r");
		for(String s : ls)
		    {
			if(s.equals("riot-waikato-072A"))
			    {
				connected = true;
				System.out.println("Connected to server");
			    }
			
		    }
	    } catch(Exception e){
		connected = false;
		//e.printStackTrace();
	    }

	    if(connected == false)
	    {
		run_command("iwlist wlan1 scan");
		
		run_command("wpa_cli -i wlan1 remove_network 0");
		run_command("wpa_cli -i wlan1 remove_network 1");
		run_command("wpa_cli -i wlan1 remove_network 2");
		run_command("wpa_cli -i wlan1 add_network");
		run_command("wpa_cli -i wlan1 set_network 0 ssid \\\"riot-waikato-072A\\\"");
		run_command("wpa_cli -i wlan1 set_network 0 psk \\\"riotwaikato\\\"");
		run_command("wpa_cli -i wlan1 enable_network 0");


		try{
		run_command("iwgetid -r");
		for(String s : ls)
		    if(s.equals("riot-waikato-072A"))
			connected = true;
		} catch(Exception e){
		    System.out.println("255");
		    //connected = false;
		}
	    }

	    System.out.println("Sleeping for 5s");
	    Thread.sleep(5000);
	    
	    if(connected == true)
	    {
		Socket conn = new Socket("169.254.72.1", 65060);
		PrintWriter out = new PrintWriter(conn.getOutputStream(), true);
		System.out.println("Connected");

		//run_command("sqlite3 tester2.db \\\" select \\* from lux;\\\"");
		//run_command("sqlite3 tester2.db \"PRAGMA journal_mode=WAL; select * from lux;\"");
		run_command("sqlite3 tester2.db \"PRAGMA journal_mode=WAL; begin; select entry_date, _dev_id, lux from lux inner join entry on lux._seq = entry.seq; delete from lux; delete from entry; commit;\"");
		for(String s : ls)
		{
		    System.out.println(s);
		    out.println(s);
		    out.flush();
		}
		conn.close();
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
