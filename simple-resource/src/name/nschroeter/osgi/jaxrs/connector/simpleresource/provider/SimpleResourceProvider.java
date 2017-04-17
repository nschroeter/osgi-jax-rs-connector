package name.nschroeter.osgi.jaxrs.connector.simpleresource.provider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import name.nschroeter.osgi.jaxrs.connector.simpleresource.HelloTO;
import name.nschroeter.osgi.jaxrs.connector.simpleresource.SimpleResource;

@Component(scope = ServiceScope.PROTOTYPE)
public class SimpleResourceProvider implements SimpleResource {

	@Override
	public HelloTO helloWorld() {
		return new HelloTO();
	}
}
