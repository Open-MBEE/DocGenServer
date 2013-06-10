package gov.nasa.jpl.mbee.mdserver;

import com.sun.grizzly.http.SelectorThread;

public class Config {
	private final static Config instance = new Config();
	
	private String tempDir;
	private String server;
	private int port;
	private String user;
	private String password;
	private int serviceport;
	private String docweb;
	private SelectorThread jersey;
	private Config() {}
	
	public static Config getInstance() {
		return instance;
	}
	public void setSelectorThread(SelectorThread s) {jersey = s;}
	public SelectorThread getSelectorThread() {return jersey;}
	public void setTempDir(String i) {tempDir = i;}
	public void setServer(String i) {server = i;}
	public void setPort(int i) {port = i;}
	public void setUser(String i) {user = i;}
	public void setPassword(String i) {password = i;}
	public String getTempDir() {return tempDir;}
	public String getServer() {return server;}
	public int getPort() {return port;}
	public String getUser() {return user;}
	public String getPassword() {return password;}
	public int getServicePort() {return serviceport;}
	public void setServicePort(int i) {serviceport = i;}
	public String getDocweb() {return docweb;}
	public void setDocweb(String d) {docweb = d;}
}
