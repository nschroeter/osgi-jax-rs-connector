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
 *    Ivan Iliev - added ServletConfigurationTracker
 *    Niels Schröter - fork and rework
 ******************************************************************************/

package name.nschroeter.osgi.jaxrs.connector.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.ws.rs.Path;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

public class Activator implements BundleActivator {

	private JaxRSConnector jaxRsConnector;
	private HttpTracker httpTracker;
	private ServletConfigurationTracker servletConfigurationTracker;
	private ApplicationConfigurationTracker applicationConfigurationTracker;
	private ServiceRegistration<?> configRegistration;
	private ServiceRegistration<JacksonFeature> jacksonFeatureRegistration;

	@Override
	public void start(BundleContext context) throws Exception {
		System.setProperty("javax.ws.rs.ext.RuntimeDelegate",
				"org.glassfish.jersey.server.internal.RuntimeDelegateImpl");
		startJerseyServer();
		jaxRsConnector = new JaxRSConnector(context);
		registerConfiguration(context);
		openHttpServiceTracker(context);
		openServletConfigurationTracker(context);
		openApplicationConfigurationTracker(context);
		ResourceListener listener = new ResourceListener(context, jaxRsConnector);
		context.addServiceListener(listener);
		JacksonFeature feature = new JacksonFeature();
		jacksonFeatureRegistration = context.registerService(JacksonFeature.class, feature, null );
	}

	private void registerConfiguration(BundleContext context) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(Constants.SERVICE_PID, Configuration.CONFIG_SERVICE_PID);
		configRegistration = context.registerService(ManagedService.class.getName(), new Configuration(jaxRsConnector),
				properties);
	}

	private void startJerseyServer() throws BundleException {
		Bundle bundle = getJerseyAPIBundle();
		if (bundle.getState() != Bundle.ACTIVE) {
			bundle.start();
		}
	}

	private void openHttpServiceTracker(BundleContext context) {
		httpTracker = new HttpTracker(context, jaxRsConnector);
		httpTracker.open();
	}

	private void openServletConfigurationTracker(BundleContext context) {
		servletConfigurationTracker = new ServletConfigurationTracker(context, jaxRsConnector);
		servletConfigurationTracker.open();
	}

	private void openApplicationConfigurationTracker(BundleContext context) {
		applicationConfigurationTracker = new ApplicationConfigurationTracker(context, jaxRsConnector);
		applicationConfigurationTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		httpTracker.close();
		servletConfigurationTracker.close();
		applicationConfigurationTracker.close();
		jacksonFeatureRegistration.unregister();
		configRegistration.unregister();
	}

	// For testing purpose
	Bundle getJerseyAPIBundle() {
		return FrameworkUtil.getBundle(Path.class);
	}
}
