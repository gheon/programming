/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.descriptor.services;

import java.io.Serializable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;


/**
 * A class implementing this interface represents a service to acquire JVMs
 * @author The ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public interface UniversalService extends Serializable {

    /**
     * Starts this Service
     * @return an array of ProActiveRuntime
     */
    public ProActiveRuntime[] startService() throws ProActiveException;

    /**
     * Returns the name of the service.
     * The name is static, it means that it is the same name for all instances of a
     * sefvice's class
     * @return the static name of the service
     */
    public String getServiceName();
}
