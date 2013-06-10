package gov.nasa.jpl.mbee.mdserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import com.nomagic.magicdraw.commandline.CommandLine;
import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;


public class CommandLineServer extends CommandLine 
{ 
	private Properties properties;
	public PrintStream console;
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH.mm.ss"); 
	
	public CommandLineServer(Properties p) {
		properties = p;
	}
	
    public static void main(String[] args) 
    { 
	    if (args.length != 1) {
		    System.out.println("Need config.properties file as first argument!");
		    System.exit(1);
	    }
	    Properties p = new Properties();
	    FileInputStream prop;
	    try {
			prop = new FileInputStream(args[0]);
	        p.load(prop);
	        System.out.println(p.toString());
	        prop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Error loading properties file.");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error loading properties file.");
			return;
		}
		if (!checkDirs(p)) {
			return;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
		
      // launch MagicDraw */
		String d = format.format(new Date());
	    System.out.println(d + " Starting magicdraw...");
		 CommandLineServer s = new CommandLineServer(p);
		 s.console = System.out; //save stdout before md changes it
		 s.launch(new String[]{});
	    
    } 
   
    private static boolean checkDirs(Properties p) {
    	boolean ok = true;
    	File temp = new File(p.getProperty("tempDir"));
    	if (!temp.exists() || !temp.canWrite()) {
    		System.out.println("The tempDir path cannot be found or write permission not valid.");
    		ok = false;
    	}
    	return ok;	
    }
    
    private void fillConfig(Properties p) {
    	Config c = Config.getInstance();
        c.setServer(p.getProperty("server"));
        c.setTempDir(p.getProperty("tempDir"));
        c.setUser(p.getProperty("user"));
        c.setPassword(p.getProperty("password"));
        c.setPort(Integer.parseInt(p.getProperty("port")));        
        c.setServicePort(Integer.parseInt(p.getProperty("serviceport")));
        c.setDocweb(p.getProperty("docweb"));
    }
    
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(Config.getInstance().getServicePort()).build();
    }

    private SelectorThread startServer() throws IOException {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", 
                "gov.nasa.jpl.mbee.mdserver");

        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(getBaseURI(), initParams);     
        return threadSelector;
    }

    protected void run() { 
	    Config c = Config.getInstance();
	    fillConfig(properties);
	    String d = format.format(new Date());
	    console.println(d + " Starting Server");
	   
	   //Server run = new Server(properties);
	    try {
			SelectorThread threadSelector = startServer();
			c.setSelectorThread(threadSelector);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(console);
			return;
		}
        Thread thread = new Thread(new Worker());
        thread.start();

        console.println(d + " Server Started");

        while(true) { //better way to do it??
    	    try {
    		    Thread.sleep(60*60*1000); //every hour
    		    memoryUsage();
    	    } catch (InterruptedException e) {
    		    e.printStackTrace();
    	    } 
        } 
	}
    
    public void memoryUsage() {
    	String d = format.format(new Date());
    	for (MemoryPoolMXBean mx : ManagementFactory.getMemoryPoolMXBeans()) {
    		
    	        console.println(d + " " + mx.getName() + ": " + mx.getUsage().getUsed()/1000/1000 + " MB");
    	    
    	}
    	console.println(d + " JVM Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000/1000 + " MB");
    	console.println();
    }
} 
