/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.activeobject.request;

import static junit.framework.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.ProActive;

import functionalTests.Helper;
/**
 * Test blocking request, and calling void, int returned type and object returned type method
 */
public class Test {
 	private static final long serialVersionUID = 5390683714407366750L;
	A activeA;
    A javaA;
    int counterActiveA;
    int counterA;

 
    @Before
	public void action() throws Exception {
        activeA = (A) ProActive.newActive(A.class.getName(), new Object[0]);
        activeA.method1();
        javaA = activeA.method2();
        counterA = javaA.method3();
        counterActiveA = activeA.method3();
    }

    
	
    @org.junit.Test
	public void postConditions() {
    	assertTrue(counterA == 1);
    	assertTrue(counterActiveA == 3);
    }
    
    @After
    public void after() {
  	  Helper.killJVMs();
    }
}
