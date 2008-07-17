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
package org.objectweb.proactive.core.descriptor.data;

import org.objectweb.proactive.core.ProActiveException;


/**
 * This class represents an array of properties that can be set at runtime when using
 * XML deployment descriptor
 * @author The ProActive Team
 * @version 1.0,  2003/04/01
 * @since   ProActive 1.0.2
 */
public class RuntimeDeploymentProperties implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 40L;
    protected java.util.ArrayList<String> runtimeProperties;

    public RuntimeDeploymentProperties() {
        runtimeProperties = new java.util.ArrayList<String>();
    }

    protected void checkProperty(String property) throws ProActiveException {
        if (!runtimeProperties.contains(property)) {
            throw new ProActiveException("This runtime property " + property +
                " does not exist or has already been set!");
        }
    }
}
