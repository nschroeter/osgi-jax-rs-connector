package name.nschroeter.osgi.jaxrs.connector.internal;

import javax.ws.rs.Path;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.Provider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/*******************************************************************************
 * Copyright (c) 2012,2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Niels Schröter - initial API and implementation 
 ******************************************************************************/

public class ResourceListener implements ServiceListener {

	private JaxRSConnector jaxRsConnector;
	private BundleContext bundleContext;

	public ResourceListener(BundleContext bundleContext, JaxRSConnector jaxRsConnector) {
		this.bundleContext = bundleContext;
		this.jaxRsConnector = jaxRsConnector;
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		ServiceReference<?> serviceReference = event.getServiceReference();
		Object service = bundleContext.getService(serviceReference);
		if (isResource(service)) {
			if (event.getType() == ServiceEvent.REGISTERED) {
				jaxRsConnector.addResource(serviceReference);
			} else if (event.getType() == ServiceEvent.UNREGISTERING) {
				jaxRsConnector.removeResource(service);
			}
		}
	}

	private boolean isResource(Object service) {
		return service != null && (hasRegisterableAnnotation(service) || service instanceof Feature);
	}

	private boolean hasRegisterableAnnotation(Object service) {
		boolean result = isRegisterableAnnotationPresent(service.getClass());
		if (!result) {
			Class<?>[] interfaces = service.getClass().getInterfaces();
			for (Class<?> type : interfaces) {
				result = result || isRegisterableAnnotationPresent(type);
			}
		}
		return result;
	}

	private boolean isRegisterableAnnotationPresent(Class<?> type) {
		return type.isAnnotationPresent(Path.class) || type.isAnnotationPresent(Provider.class);
	}
}
