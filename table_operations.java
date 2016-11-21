import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class table_operations {
    //maybe just use for testing?

    static List<String> ls;
    public static String dbname = "tester2.db";
    public static void main(String[] args) {
	try {
	    int k = register_user("lob");	    
	    if(k == 0) {
		String user_id = ls.get(ls.size() -1);
		k = register_device(Integer.parseInt(user_id), "0x" + user_id);
		if(k == 0) {
		    insert_motion_entry("0x"+user_id, new float[]{0,0,0,1,2,3,6,5,4}, "1234125");
		}
	    }
	} catch (Exception e) {
	    System.err.println(e);
	    System.err.println("invalid");
	    
	}
    }
    
    public static int insert_lux_entry(String dev_id, String val, String timestamp, boolean DEBUG) throws Exception {
	String st = "./Add_Lux.sh " + dbname + " " + dev_id + " " + timestamp + " " +val;
	if(DEBUG) System.out.println(st);
	int rev = run_command(st);
	return rev;
    }
    
    public static int insert_motion_entry(String dev_id, String S, String date) throws Exception {
	String st = "./Add_Motion.sh " + dbname + " " + dev_id + " " + date + " ";
	st += S;
	System.out.println(st);
	int rv = run_command(st);
	return rv;
    }

    public static int insert_motion_entry(String dev_id, float[] set, String date) throws Exception {
	String st = "./Add_Motion.sh " + dbname + " " + dev_id + " " + date;
	for(int i = 0; i < 9; i++)
	    st += " " + set[i];
	System.out.println(st);
	int rv = run_command(st);
	return rv;
    }
    
    public static int register_user(String name) throws Exception {
	String st = "./register_user.sh " + dbname + " " + name;
	System.out.println(st);
	int rv = run_command(st);
	print_list(ls);
	return rv;
    }

    public static int register_device(int user, String dev) throws Exception {
	String st = "./register_device.sh " + dbname + " " + user + " " + dev;
	System.out.println(st);
	int rv = run_command(st);
	print_list(ls);
	return rv;
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
