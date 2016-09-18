import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class table_operations {
    //maybe just use for testing?

    static List<String> ls;
    public static String dbname = "test.db";
    public static void main(String[] args) {
	try {
	    register_user("job");	    
	} catch (Exception e) {
	    System.err.println(e);
	    System.err.println("invalid");
	    print_list(ls);
	}
    }
    

    public static int register_user(String name) throws Exception {
	String st = "./register_user.sh " + dbname + " " + name;
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
