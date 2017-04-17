package name.nschroeter.osgi.jaxrs.connector.internal;

/*******************************************************************************
 * Copyright (c) 2015 Ivan Iliev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ivan Iliev - initial API and implementation
 *    Holger Staudacher  - ongoing development
 *    Niels Schröter - fork and rework
 ******************************************************************************/

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import name.nschroeter.osgi.jaxrs.connector.ServletConfiguration;

public class ServletConfigurationTracker extends ServiceTracker<ServletConfiguration, ServletConfiguration> {

	private final JaxRSConnector connector;

	ServletConfigurationTracker(BundleContext context, JaxRSConnector connector) {
		super(context, ServletConfiguration.class.getName(), null);
		this.connector = connector;
	}

	@Override
	public ServletConfiguration addingService(ServiceReference<ServletConfiguration> reference) {
		return connector.setServletConfiguration(reference);
	}

	@Override
	public void removedService(ServiceReference<ServletConfiguration> reference, ServletConfiguration service) {
		connector.unsetServletConfiguration(reference, (ServletConfiguration) service);
	}
}
