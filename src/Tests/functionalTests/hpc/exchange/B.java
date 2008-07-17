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
package functionalTests.hpc.exchange;

import org.objectweb.proactive.api.PAActiveObject;


public class B {
    public static int HALF_SIZE = 1000;

    private ComplexDoubleArray array1, array2;

    public B() {
    }

    public void start(int id, B b1, B b2, B b3) {

        if (id < 3) {
            // Perform a *local* exchange between b1 and b2
            this.array1 = new ComplexDoubleArray(2 * HALF_SIZE, id == 2);
            PAActiveObject.exchange("local", id == 1 ? b2 : b1, array1, array1);
        }

        if (id > 1) {
            // Perform a *distant* exchange between b2 and b3
            this.array2 = new ComplexDoubleArray(2 * HALF_SIZE, id == 3);
            PAActiveObject.exchange("distant", id == 2 ? b3 : b2, array2, array2);
        }
    }

    public double getChecksum1() {
        return array1.getChecksum();
    }

    public double getChecksum2() {
        return array2.getChecksum();
    }
}
