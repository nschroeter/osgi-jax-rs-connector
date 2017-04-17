package name.nschroeter.osgi.jaxrs.connector.simpleresource.provider;

import org.osgi.service.component.annotations.Component;

import name.nschroeter.osgi.jaxrs.connector.simpleresource.SimpleResource;

@Component
public class SimpleResourceProvider implements SimpleResource {

	@Override
	public String helloWorld() {
		return "Hello World!";
	}

}
