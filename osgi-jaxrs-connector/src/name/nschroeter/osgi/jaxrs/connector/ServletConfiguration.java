package name.nschroeter.osgi.jaxrs.connector;

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

import java.util.Dictionary;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

public interface ServletConfiguration {

  HttpContext getHttpContext( HttpService httpService, String rootPath );

  Dictionary<String, String> getInitParams( HttpService httpService, String rootPath );
  
}

