package name.nschroeter.osgi.jaxrs.connector.internal;


/*******************************************************************************
 * Copyright (c) 2015 Holger Staudacher and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    Niels Schröter - fork and rework
 ******************************************************************************/

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import name.nschroeter.osgi.jaxrs.connector.ApplicationConfiguration;

public class ApplicationConfigurationTracker
		extends ServiceTracker<ApplicationConfiguration, ApplicationConfiguration> {

	private final JaxRSConnector connector;

	ApplicationConfigurationTracker(BundleContext context, JaxRSConnector connector) {
		super(context, ApplicationConfiguration.class.getName(), null);
		this.connector = connector;
	}

	@Override
	public ApplicationConfiguration addingService(ServiceReference<ApplicationConfiguration> reference) {
		return connector.addApplicationConfiguration(reference);
	}

	@Override
	public void removedService(ServiceReference<ApplicationConfiguration> reference, ApplicationConfiguration service) {
		connector.removeApplicationConfiguration(reference, service);
	}
}
