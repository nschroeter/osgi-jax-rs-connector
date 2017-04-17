package name.nschroeter.osgi.jaxrs.connector.internal;

/*******************************************************************************
 * Copyright (c) 2012,2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    ProSyst Software GmbH. - compatibility with OSGi specification 4.2 APIs
 *    Ivan Iliev - Performance Optimizations
 *    Niels Schröter - fork and rework
 ******************************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import name.nschroeter.osgi.jaxrs.connector.ApplicationConfiguration;
import name.nschroeter.osgi.jaxrs.connector.ServletConfiguration;
import name.nschroeter.osgi.jaxrs.connector.internal.ServiceContainer.ServiceHolder;

public class JaxRSConnector {

	static final String CONFIG_SERVICE_PID = "com.eclipsesource.jaxrs.connector";
	static final String PROPERTY_ROOT = "root";
	static final String PROPERTY_PUBLISH_DELAY = "publishDelay";
	static final long DEFAULT_PUBLISH_DELAY = 150;

	private static final String HTTP_SERVICE_PORT_PROPERTY = "org.osgi.service.http.port";
	private static final String RESOURCE_HTTP_PORT_PROPERTY = "http.port";
	private static final String DEFAULT_HTTP_PORT = "80";

	private final Object lock = new Object();
	private final ServiceContainer httpServices;
	private final ServiceContainer resources;
	private final Map<HttpService, JerseyContext> contextMap;
	private final BundleContext bundleContext;
	private final List<ServiceHolder> resourceCache;
	private ServletConfiguration servletConfiguration;
	private final ServiceContainer applicationConfigurations;
	private Configuration configuration;

	JaxRSConnector(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		this.configuration = new Configuration(this);
		this.httpServices = new ServiceContainer(bundleContext);
		this.resources = new ServiceContainer(bundleContext);
		this.contextMap = new HashMap<HttpService, JerseyContext>();
		this.resourceCache = new ArrayList<ServiceHolder>();
		this.applicationConfigurations = new ServiceContainer(bundleContext);
	}

	void updateConfiguration(Configuration configuration) {
		synchronized (lock) {
			this.configuration = configuration;
			doUpdateConfiguration(configuration);
		}
	}

	HttpService addHttpService(ServiceReference<?> reference) {
		synchronized (lock) {
			return doAddHttpService(reference);
		}
	}

	ServletConfiguration setServletConfiguration(ServiceReference<?> reference) {
		if (servletConfiguration == null) {
			servletConfiguration = (ServletConfiguration) bundleContext.getService(reference);
			doUpdateServletConfiguration();
			return servletConfiguration;
		}
		return null;
	}

	void unsetServletConfiguration(ServiceReference<?> reference, ServletConfiguration service) {
		if (servletConfiguration == service) {
			servletConfiguration = null;
			bundleContext.ungetService(reference);
			doUpdateServletConfiguration();
		}
	}

	ApplicationConfiguration addApplicationConfiguration(ServiceReference<?> reference) {
		synchronized (lock) {
			ApplicationConfiguration service = (ApplicationConfiguration) applicationConfigurations.add(reference)
					.getService();
			doUpdateAppConfiguration();
			return service;
		}
	}

	void removeApplicationConfiguration(ServiceReference<?> reference, ApplicationConfiguration service) {
		synchronized (lock) {
			applicationConfigurations.remove(service);
			doUpdateAppConfiguration();
		}
	}

	private void doUpdateServletConfiguration() {
		ServiceHolder[] services = httpServices.getServices();
		for (ServiceHolder serviceHolder : services) {
			contextMap.get(serviceHolder.getService()).updateServletConfiguration(servletConfiguration);
		}
	}

	private void doUpdateAppConfiguration() {
		ServiceHolder[] services = httpServices.getServices();
		for (ServiceHolder serviceHolder : services) {
			contextMap.get(serviceHolder.getService()).updateAppConfiguration(applicationConfigurations);
		}
	}

	private void doUpdateConfiguration(Configuration configuration) {
		ServiceHolder[] services = httpServices.getServices();
		for (ServiceHolder serviceHolder : services) {
			contextMap.get(serviceHolder.getService()).updateConfiguration(configuration);
		}
	}

	HttpService doAddHttpService(ServiceReference<?> reference) {
		ServiceHolder serviceHolder = httpServices.add(reference);
		HttpService service = (HttpService) serviceHolder.getService();
		contextMap.put(service, createJerseyContext(service, configuration, servletConfiguration));
		clearCache();
		return service;
	}

	private void clearCache() {
		List<ServiceHolder> cache = new ArrayList<ServiceHolder>(resourceCache);
		resourceCache.clear();
		for (ServiceHolder serviceHolder : cache) {
			registerResource(serviceHolder);
		}
	}

	void removeHttpService(HttpService service) {
		synchronized (lock) {
			doRemoveHttpService(service);
		}
	}

	void doRemoveHttpService(HttpService service) {
		JerseyContext context = contextMap.remove(service);
		if (context != null) {
			cacheFreedResources(context);
		}
		httpServices.remove(service);
	}

	private void cacheFreedResources(JerseyContext context) {
		List<Object> freeResources = context.eliminate();
		for (Object resource : freeResources) {
			resourceCache.add(resources.find(resource));
		}
	}

	Object addResource(ServiceReference<?> reference) {
		synchronized (lock) {
			return doAddResource(reference);
		}
	}

	private Object doAddResource(ServiceReference<?> reference) {
		ServiceHolder serviceHolder = resources.add(reference);
		registerResource(serviceHolder);
		return serviceHolder.getService();
	}

	private void registerResource(ServiceHolder serviceHolder) {
		Object port = getPort(serviceHolder);
		registerResource(serviceHolder, port);
	}

	private Object getPort(ServiceHolder serviceHolder) {
		ServiceReference<?> reference = serviceHolder.getReference();
		Object port = reference.getProperty(RESOURCE_HTTP_PORT_PROPERTY);
		if (port == null) {
			port = bundleContext.getProperty(HTTP_SERVICE_PORT_PROPERTY);
			if (port == null) {
				port = DEFAULT_HTTP_PORT;
			}
		}
		return port;
	}

	private void registerResource(ServiceHolder serviceHolder, Object port) {
		HttpService service = findHttpServiceForPort(port);
		if (service != null) {
			JerseyContext jerseyContext = contextMap.get(service);
			jerseyContext.addResource(serviceHolder.getService());
		} else {
			cacheResource(serviceHolder);
		}
	}

	private void cacheResource(ServiceHolder serviceHolder) {
		resourceCache.add(serviceHolder);
	}

	private HttpService findHttpServiceForPort(Object port) {
		ServiceHolder[] serviceHolders = httpServices.getServices();
		HttpService result = null;
		for (ServiceHolder serviceHolder : serviceHolders) {
			Object servicePort = getPort(serviceHolder);
			if (servicePort.equals(port)) {
				result = (HttpService) serviceHolder.getService();
			}
		}
		return result;
	}

	void removeResource(Object resource) {
		synchronized (lock) {
			doRemoveResource(resource);
		}
	}

	private void doRemoveResource(Object resource) {
		ServiceHolder serviceHolder = resources.find(resource);
		resourceCache.remove(serviceHolder);
		Object port = getPort(serviceHolder);
		HttpService httpService = findHttpServiceForPort(port);
		removeResourcesFromContext(resource, httpService);
		resources.remove(resource);
	}

	private void removeResourcesFromContext(Object resource, HttpService httpService) {
		JerseyContext jerseyContext = contextMap.get(httpService);
		if (jerseyContext != null) {
			jerseyContext.removeResource(resource);
		}
	}

	// For testing purpose
	JerseyContext createJerseyContext(HttpService service, Configuration configuration,
			ServletConfiguration servletConfiguration) {
		return new JerseyContext(service, configuration, servletConfiguration, applicationConfigurations);
	}
}