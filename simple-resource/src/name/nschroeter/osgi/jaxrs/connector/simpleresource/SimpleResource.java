package name.nschroeter.osgi.jaxrs.connector.simpleresource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public interface SimpleResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	String sayHello();
}
