package gov.nasa.jpl.mbee.mdserver;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.project.RemoteProjectDescriptor;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;

@Path("/docgen")
public class ServerResource {

	@PUT @Path("/request/{ticket}")
	@Produces("text/plain")
	@Consumes("application/x-www-form-urlencoded")
	public String request(@PathParam("ticket") String ticket, MultivaluedMap<String, String> form) {
		//String category  = form.getFirst("category");
		String project = form.getFirst("project");
		String doc = form.getFirst("package");
		String user = form.getFirst("user");
		int web2 = Integer.parseInt(form.getFirst("web2"));
		RequestQueue q = RequestQueue.getInstance();
    	Request r = new Request(project, doc, user, ticket, web2);
    	
    	if (q.offer(r)) {
    		RequestMap.getInstance().putIfAbsent(ticket, r);
    		return ticket;
    	}
    	return "Full";
		
	}

	
	@GET @Path("/queuelist")
	@Produces("text/plain")
	public String getQueueList() {
		JSONArray result = new JSONArray();
		Request r = RequestMap.getInstance().get("current");
		if (r != null)
			result.add(r.getTicket());
    	Iterator<Request> i = RequestQueue.getInstance().iterator();
    	while(i.hasNext()) {
    		Request r2 = i.next();
    		result.add(r2.getTicket());
    	}
    	return result.toJSONString();
    }
    
	@DELETE @Path("/request/{ticket}")
	@Produces("text/plain")
    public String removeRequest(@PathParam("ticket") String ticket) {
    	RequestMap m = RequestMap.getInstance();
    	if(m.containsKey(ticket)) {
    		Request r = m.get(ticket);
    		RequestQueue q = RequestQueue.getInstance();
    		if (q.remove(r)) {
    			m.remove(ticket);
    			return "1";
    		}
    	}
    	return "0";
    }

	@GET @Path("/projects")
	@Produces("text/plain")
    public String getProjects() {
		JSONObject catp = new JSONObject();
    	Config c = Config.getInstance();
    	try {
    		String user = TeamworkUtils.getLoggedUserName();
    		if (user == null || user.equals(""))
    			if (!TeamworkUtils.login(c.getServer(), c.getPort(), c.getUser(), c.getPassword()))
    				return "{}";
    		Map<String, String> categories = TeamworkUtils.getCategories();
    		for(String id: categories.keySet()) {
    			Collection<RemoteProjectDescriptor> ps = TeamworkUtils.getCategoryProjects(id);
    			JSONArray projects = new JSONArray();
    			for(RemoteProjectDescriptor pd: ps) {
    				projects.add(pd.getRepresentationString());
    			}
    			catp.put(categories.get(id), projects);
    		}
    	} catch(RemoteException e) {
    		e.printStackTrace();
    	}
    	return catp.toJSONString();
    }
}