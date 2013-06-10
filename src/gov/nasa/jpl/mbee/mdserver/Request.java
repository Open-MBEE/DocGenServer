package gov.nasa.jpl.mbee.mdserver;
import java.io.Serializable;
import java.util.Date;

// see https://jplwiki.jpl.nasa.gov:8443/display/OpsRev/DocGen+Web+Service+Interface

public class Request implements Serializable {
	
	private String ticket;
	private String project;
	private String pack;
	private int status;
	private Date received;
	private Date started;
	private String log;
	private String user;
	private int web2;
	private String xslparams;
	
	Request() {}
	
	Request(String project, String pack, String user, String ticket, int web2) {
		//SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH.mm.ss");
		this.project = project;
		this.pack = pack;
		this.user = user;
		Date now = new Date();
		this.web2 = web2;
		this.ticket = ticket;
		received = now;
		status = 0;
		xslparams = "";
	}

	public void setTicket(String t) {ticket = t;}
	
	public void setProject(String p) {project = p;}
	
	public void setPackage(String p) {pack = p;}
	
	public String getTicket() {return ticket;}
	
	public String getProject() {return project;}
	
	public String getPackage() {return pack;}
	
	public int getStatus() { return status;}
	
	public void setStatus(int s) {status = s;}
	
	public Date getReceived() {return received;}
	
	public Date getStarted() {return started;}
		
	public void setStarted(Date time) {started = time;}
		
	public void setReceived(Date time) {received = time;}
	
	public String getLog() {return log;}
	
	public void setLog(String l) {log = l;}

	public String getUser() {return user;}
	
	public void setUser(String u) {user = u;}

	
	public int getWeb2() {return web2;}
	public void setWeb2(int b) {web2 = b;}
	
        public static final int DOCGEN_ONLY = 0;
        public static final int BOTH = 1;
        public static final int WEB2_ONLY = 2;
	public boolean usedDocGen() {return web2 == BOTH || web2 == DOCGEN_ONLY;}
	public boolean usedWeb2() {return web2 == BOTH || web2 == WEB2_ONLY;}
	public void setXslParams(String s) {xslparams = s;}
	public String getXslParams() {return xslparams;}

}
