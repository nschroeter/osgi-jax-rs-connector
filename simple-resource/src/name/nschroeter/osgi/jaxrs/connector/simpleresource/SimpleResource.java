package name.nschroeter.osgi.jaxrs.connector.simpleresource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public interface SimpleResource {

	@GET
	String helloWorld();
}
