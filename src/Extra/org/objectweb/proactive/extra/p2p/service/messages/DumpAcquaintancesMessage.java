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
package org.objectweb.proactive.extra.p2p.service.messages;

import java.io.Serializable;

import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.util.UniversalUniqueID;


public class DumpAcquaintancesMessage extends BreadthFirstMessage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 40L;

    public DumpAcquaintancesMessage() {
    }

    public DumpAcquaintancesMessage(int ttl, UniversalUniqueID id, P2PService sender) {
        super(ttl, id, sender);
    }

    @Override
    public void execute(P2PService target) {
        //        try {
        System.out.println("***** " + P2PService.getHostNameAndPortFromUrl(target.getAddress().toString()) +
            " *****");
        //        } catch (UnknownHostException e) {
        //            e.printStackTrace();
        //        }
        String[] v = target.getAcquaintanceManager().getAcquaintancesURLs().toArray(new String[] {});

        for (int i = 0; i < v.length; i++) {
            System.out.println(v[i]);
        }

        System.out.println("*****  *****");
        System.out.println("DumpAcquaintancesMessage.execute() " + TTL);
    }
}
