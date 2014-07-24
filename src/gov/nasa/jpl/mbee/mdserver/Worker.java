package gov.nasa.jpl.mbee.mdserver;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSerializeVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.ci.persistence.mounting.IMountPoint;
import com.nomagic.ci.persistence.sharing.ISharePoint;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.magicreport.GenerateTask;
import com.nomagic.magicdraw.magicreport.helper.TemplateHelper;
import com.nomagic.magicdraw.magicreport.ui.bean.ReportBean;
import com.nomagic.magicdraw.magicreport.ui.bean.TemplateBean;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
//import com.nomagic.magicdraw.teamwork2.ServerLoginInfo;
//import com.nomagic.magicdraw.teamwork2.TeamworkService;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Worker implements Runnable {
	private final static SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH.mm.ss");
	private List<String> profiles;
	private Stereotype documentView;
	private Config c;

	public Worker() {
		c = Config.getInstance();
		profiles = new ArrayList<String>();
		profiles.add("UML_Standard_Profile");
		profiles.add("SysML Profile");
		profiles.add("Document Profile");
		profiles.add("Common Extensions");
		try {
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		RequestQueue q = RequestQueue.getInstance();
		System.out.println("Worker Thread Started!");
		
		while(true) {
			Request r;
			try {
				r = q.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			generate(r);
			System.gc();
		}
	}

	private void generate(Request r) {
		RequestMap.getInstance().put("current", r);
		String project = r.getProject();
		String pack = r.getPackage();
		String user = TeamworkUtils.getLoggedUserName();
		boolean loggedin = true;
		
		Date now = new Date();
    	String time = format.format(now);
    	r.setStarted(now);
    	r.setStatus(1);
    	String dir = c.getTempDir() + File.separator + r.getTicket();
    	String docbookdir = dir + File.separator + "docbook";
    	String web2dir = dir + File.separator + "web2";
    	boolean created2 = (new File(web2dir)).mkdirs();
    	boolean created = (new File(docbookdir)).mkdirs();

    	PrintWriter log = null;
    	try {
    		if (!created) {
				System.out.println("Cannot create output directory for request " + r.getTicket() + ". Aborted.");
				return;
			}
			try {
				log = new PrintWriter(new FileOutputStream(dir + File.separator + "request.log"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			r.setLog(dir + File.separator + "request.log");
			
			log.println("[INFO] Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000/1000 + " MB");
			log.println(time + " [INFO] Request started.");
			
	    	if (user == null || user.equals(""))
	    		try {
	    			log.println(format.format(new Date()) + " [INFO] Logging in to teamwork.");
	    			//ServerLoginInfo sli = new ServerLoginInfo(c.getServer() + ":" + c.getPort(), c.getUser(), c.getPassword(), true);
	    			//TeamworkService ts = TeamworkService.
	    			loggedin = TeamworkUtils.login(c.getServer(), c.getPort(), c.getUser(), c.getPassword());
	    		} catch (Exception e) {
	    			log.println(format.format(new Date()) + " [ERROR] Cannot connect to teamwork. Aborted.");
	    			return;
	    		}
	    	if (!loggedin) {
	    		log.println(format.format(new Date()) + " [ERROR] Cannot connect to teamwork. Aborted.");
	    		return;
	    	}
	        ProjectDescriptor pd = null;
	        try {
	        	log.println(format.format(new Date()) + " [INFO] Getting project descriptor from teamwork.");
	            pd = TeamworkUtils.getRemoteProjectDescriptorByQualifiedName(project);
	        } catch (RemoteException e) {
	            e.printStackTrace(log);
	            return;
	        }
	        if (pd == null) {
	        	log.println(format.format(new Date()) + " [ERROR] Cannot get project descriptor " + project + ". Aborted.");
	        	return;
	        }
	        ProjectsManager pm = Application.getInstance().getProjectsManager();
	        try {
	        	log.println(format.format(new Date()) + " [INFO] Loading project.");
	        	pm.loadProject(pd, false);
	        	pm.setActiveProject(pm.getProjects().get(0));
	        	Model m = pm.getActiveProject().getModel();

	        	if (r.getWeb2() < 2) //0 is docgen, 1 is docgen and web2, 2 is web2 only
	        		handleDocgen(r, m, log, project, docbookdir);
	        	if (r.getWeb2() > 0)
	        		handleWeb2(r, m, log, project, web2dir);
	        	//handlePpt(r, m, log, dir);
	        } catch (Exception e) {
	        	e.printStackTrace(log);
	        } finally {
	        	pm.closeProject();
	        	System.gc(); //try to get back as much memory as possible
	        }
    	} catch (Exception e) {
    		if (log != null)
    			e.printStackTrace(log);
    		else
    			e.printStackTrace();
    	} finally {  
    		Date fintime = new Date();
	    	if (log != null) {
	    		//log.println(format.format(fintime) + " [INFO] Docbook Generation Finished");
	    		log.println("[INFO] Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000/1000 + " MB");
	    	}
	    	File from = new File(dir);
    		upload(from, r, log);
	    	r.setStatus(2);
	    	RequestMap.getInstance().remove("current");
	    	Utils.deleteDir(from);
	    }
    }
	
	private Element findDocGen3Element(Element e, String pack) {
		Element res = null;
		if (pack.equals("")) {
			if (StereotypesHelper.hasStereotype(e, DocGen3Profile.documentStereotype) || StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
				res = e;
			} else {
				for (Element ee: e.getOwnedElement()) {
					res = findDocGen3Element(ee, pack);
					if (res != null)
						break;
				}
			}
		} else {
			if ((StereotypesHelper.hasStereotype(e, DocGen3Profile.documentStereotype) || StereotypesHelper.hasStereotypeOrDerived(e, documentView)) && ((NamedElement)e).getName().equals(pack)) {
				res = e;
			} else {
				for (Element ee: e.getOwnedElement()) {
					res = findDocGen3Element(ee, pack);
					if (res != null)
						break;
				}
			}
		}
		return res;
	}
	
	private Element findElementForDocGen3(List<Package> projectPackages, String pack) {
		Element res = null;
		for (Package p: projectPackages) {
			res = findDocGen3Element(p, pack);
			if (res != null)
				break;
		}
		return res;
	}
	
	private String getDocGen3Params(Document dge) {
		JSONObject params = new JSONObject();
		params.put("chunk.first.sections", dge.getChunkFirstSections() ? "1" : "0");
		params.put("chunk.section.depth", Integer.toString(dge.getChunkSectionDepth()));
		
		params.put("toc.section.depth", Integer.toString(dge.getTocSectionDepth()));
		if (dge.getHeader() != null)
			params.put("jpl.header", dge.getHeader());
		if (dge.getFooter() != null)
			params.put("jpl.footer", dge.getFooter());
		if (dge.getSubheader()  != null)
			params.put("jpl.subheader", dge.getSubheader());
		if (dge.getSubfooter() != null)
			params.put("jpl.subfooter", dge.getSubfooter());
		
		params.put("body.start.indent", "0pt");
		params.put("section.autolabel", "1");
		params.put("section.label.includes.component.label", "1");
		params.put("fop1.extensions", "1");
		params.put("html.stylesheet", "docgen.css");
		params.put("chunk.tocs.and.lots", "1");
		params.put("chunk.tocs.and.lots.has.title", "0");
		
		params.put("title", dge.getTitle());
		return params.toString();	
	}
	
	private void handleDocgen3(Request r, Model m, PrintWriter log, String project, String docbookdir, Element doc) {
		try {
			log.println(format.format(new Date()) + "[INFO] Validating docgen 3 document");
			gov.nasa.jpl.mbee.generator.DocumentValidator dv = new gov.nasa.jpl.mbee.generator.DocumentValidator(doc);
			dv.validateDocument();
			dv.printErrors(log);
			if (dv.isFatal()) {
				log.println("[FATAL] The docgen 3 document has fatal loops in it, aborting.");
				return;
			}
			log.println(format.format(new Date()) + " [INFO] Generating docgen 3 document " + ((NamedElement)doc).getName());
			DocumentGenerator dg = new DocumentGenerator(doc, dv, log);
			Document dge = dg.parseDocument();
			//dg.printErrors(log);
			(new PostProcessor()).process(dge);
			File savefile = new File(docbookdir + File.separator + "out.xml");
			File dir = new File(docbookdir);
			
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(savefile));
				//List<DocumentElement> books = dge.getDocumentElement();
				DocBookOutputVisitor visitor = new DocBookOutputVisitor(false, docbookdir);
				dge.accept(visitor);
				DBBook book = visitor.getBook();
				if (book != null) {
					DBSerializeVisitor v = new DBSerializeVisitor(true, dir, null);
					//String s = books.get(0).serializeToDocbook(dir, genNewImage);
					log.println("[INFO] Writing DocGen 3 document.");
					book.accept(v);
					writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
					writer.write(v.getOut());
				}
				writer.flush();
				writer.close();
			} catch (IOException ex) {
				ex.printStackTrace(log);
			}
			r.setXslParams(getDocGen3Params(dge));	
		} catch (Exception ex) {
			ex.printStackTrace(log);
		}
		if (r.getPackage().equals(""))
			r.setPackage(((NamedElement)doc).getName());
	}
	
	private void handleDocgen(Request r, Model m, PrintWriter log, String project, String docbookdir) {
		log.println(format.format(new Date()) + " [INFO] Finding Document.");
		String pack = r.getPackage();
		List<Package> projectPackages = getProjectPackageElements(m); 
		documentView = StereotypesHelper.getStereotype(Project.getProject(m), DocGen3Profile.documentViewStereotype,
	            "Document Profile");
		if (documentView == null) {
		    log.println(format.format(new Date()) + " [ERROR] Document Profile's Product stereotype not found. Aborted");
		    return;
		}
		Element docgen3e = findElementForDocGen3(projectPackages, pack);
        if (docgen3e == null) {
        	log.println(format.format(new Date()) + " [ERROR] Cannot find DocGen 3 element, Aborted.");
        	return;
        } 
        handleDocgen3(r, m, log, project, docbookdir, docgen3e);
	}
	
	//get only packages that're actually in this model's project
	private List<String> getProjectPackages(Model m) {
		List<String> packages = new ArrayList<String>();
		for (Package p: getProjectPackageElements(m)) {
				packages.add(p.getID());
		}
		return packages;
	}
	
	private List<Package> getProjectPackageElements(Model m) {
		Project p = Project.getProject(m);
		Set<Package> shared = new HashSet<Package>();
		for (IAttachedProject iap: ProjectUtilities.getAllAttachedProjects(p)) {
			shared.addAll(ProjectUtilities.getSharedPackagesIncludingResharedRecursively(iap));
		}
		List<Package> owned = new ArrayList<Package>();
		for (Package pack: m.getNestedPackage()) {
			if (!shared.contains(pack))
				owned.add(pack);
		}
		return owned;
	}
	
	private void handlePpt(Request r, Model m, PrintWriter log, String dir) {
		log.println(format.format(new Date()) + " [INFO] Finding ppt template.");
	    TemplateBean templateBean = null;
	    List<TemplateBean> templateList = TemplateHelper.listTemplates();
	    for (int i = 0; i < templateList.size(); i++) {
	        String name = templateList.get(i).getName();
	        if (name.equals("Diagram Presentation PowerPoint")) {
	            templateBean = templateList.get(i);
	        } 
	    }
	    if (templateBean == null) {
	    	log.println(format.format(new Date()) + " [ERROR] Cannot find ppt report template. Aborted.");
	    	return;
	    }
	    ReportBean reportBean = null;
		try {
			log.println(format.format(new Date()) + " [INFO] Cloning default report settings.");
			reportBean = (ReportBean)templateBean.getDefaultReport().clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(log);
			return;
		}
		List<Package> projectPackages = getProjectPackageElements(m);
    	String[] packageIds = new String[projectPackages.size()];
    	for (int i=0; i < projectPackages.size(); i++)
    		packageIds[i] = projectPackages.get(i).getID();
		templateBean.setSelectedReport(reportBean);
    	reportBean.getSelectedPackage().setPackageIds(packageIds);	
    	reportBean.getReportProperty().setOutputFile(dir + File.separator + "diagrams.pptx");
    	reportBean.getReportProperty().setOutputImageFormat("emf");

		try {
			log.println(format.format(new Date()) + " [INFO] Generating pptx output.");
			GenerateTask gt = new GenerateTask(templateBean);
			gt.execute();
			log.println(format.format(new Date()) + " [INFO] Finished generating pptx output.");
			
		} catch (Exception e) {
			e.printStackTrace(log);
			return;
		}
		
	}
	
	private void handleWeb2(Request r, Model m, PrintWriter log, String project, String web2dir) {
        log.println(format.format(new Date()) + " [INFO] Finding Web Publisher 2.0 template.");
	    TemplateBean templateBean = null;
	    List<TemplateBean> templateList = TemplateHelper.listTemplates();
	    for (int i = 0; i < templateList.size(); i++) {
	        String name = templateList.get(i).getName();
	        if (name.equals("Web Publisher 2.0")) {
	            templateBean = templateList.get(i);
	        } 
	    }
	    if (templateBean == null) {
	    	log.println(format.format(new Date()) + " [ERROR] Cannot find web2 report template. Aborted.");
	    	return;
	    }

	    ReportBean reportBean = null;
		try {
			log.println(format.format(new Date()) + " [INFO] Cloning default report settings.");
			reportBean = (ReportBean)templateBean.getDefaultReport().clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(log);
			return;
		}
		String index = web2dir + File.separator + "index.html";
	    templateBean.setSelectedReport(reportBean);
	    List<String> generatePackages = getProjectPackages(m); //get packages that're not from mounted modules

	    if (generatePackages.size() == 0)
	    	reportBean.getSelectedPackage().setPackageIds(new String[]{m.getID()});
	    else
	    	reportBean.getSelectedPackage().setPackageIds(generatePackages.toArray(new String[1]));	
	    reportBean.getReportProperty().setOutputFile(index);

		try {
			log.println(format.format(new Date()) + " [INFO] Generating web2 output.");
			GenerateTask gt = new GenerateTask(templateBean);
			gt.execute();
			log.println(format.format(new Date()) + " [INFO] Finished generating web2 output.");
		} catch (Exception e) {
			e.printStackTrace(log);
			return;
		}
	}
	
	private void upload(File dir, Request r, PrintWriter log) {
		log.println(format.format(new Date()) + " [INFO] Uploading zip file to docweb.");
		log.flush();
		log.close();
		File out = new File(c.getTempDir() + File.separator + r.getTicket() + ".zip");
		try {
			Utils.zipFolder(dir, out);
			FormDataMultiPart form = new FormDataMultiPart();
			form.bodyPart(new FileDataBodyPart("zip", out));
			form.field("requestId", r.getTicket());
			form.field("xslparams", r.getXslParams());
			form.field("received", format.format(r.getReceived()));
			form.field("started", format.format(r.getStarted()));
			form.field("document", r.getPackage());
			
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
			    public X509Certificate[] getAcceptedIssuers(){return null;}
			    public void checkClientTrusted(X509Certificate[] certs, String authType){}
			    public void checkServerTrusted(X509Certificate[] certs, String authType){}
			}};

			// Install the all-trusting trust manager
			
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			ClientConfig config = new DefaultClientConfig();
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(
			     new HostnameVerifier() {
			         @Override
			         public boolean verify( String s, SSLSession sslSession ) {
				           return true;
			         }
			     }, sc
			 ));

		    WebResource webResource = Client.create(config).resource(c.getDocweb() + r.getTicket() + "/");
		    String response = webResource.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.TEXT_PLAIN).post(String.class, form);//get(String.class);//post(String.class, form); //how to get around django's csrf thing
		} catch (Exception e) {
			e.printStackTrace();
		}		
		out.delete();
	}
}
