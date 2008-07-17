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
package org.objectweb.proactive.core.component.componentcontroller;

import org.objectweb.fractal.api.control.NameController;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.ControllerStateDuplication;
import org.objectweb.proactive.core.component.controller.ControllerState;


/**
 * Implementation of the name controller component
 * @author The ProActive Team
 *
 */
public class NameControllerComponent extends AbstractProActiveComponentController implements NameController,
        ControllerStateDuplication {

    /**
     * 
     */
    private static final long serialVersionUID = 40L;
    private String name;

    public String getFcName() {
        return name;
    }

    public void setFcName(String name) {
        this.name = name;

    }

    public void duplicateController(Object c) {
        if (c instanceof String) {
            name = (String) c;

        } else {
            throw new ProActiveRuntimeException(
                "ProActiveNameController : Impossible to duplicate the controller " + this +
                    " from the controller" + c);
        }

    }

    public ControllerState getState() {

        return new ControllerState(name);
    }

}
