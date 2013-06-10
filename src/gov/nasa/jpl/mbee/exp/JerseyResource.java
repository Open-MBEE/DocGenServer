package gov.nasa.jpl.mbee.exp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/helloworld")
public class JerseyResource {

	@GET
	@Produces("text/plain")
	public String getMessage() {
		return "Hello World";
	}
}
