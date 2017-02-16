/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionalTests.annotations.activeobject.inputs;

import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class InnerClasses {

    protected javax.swing.JSplitPane verticalSplitPane;

    public InnerClasses() {
    }

    // inner class
    class Dada {
    }

    // inner class
    // ERROR
    @ActiveObject
    class AnnotatedDada {
    }

    public void localInnerClass() {
        // local inner class
        class InnerClass {
        }

        // local inner class
        // ERROR - unfortunately cannot be checked!
        @ActiveObject
        class AnnotatedInnerClass {
        }
    }

    // anonymous inner class
    public InnerClasses(String name, Integer width, Integer height) {
        verticalSplitPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
            }
        });
    }
}