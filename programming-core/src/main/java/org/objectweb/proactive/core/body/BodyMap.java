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
package org.objectweb.proactive.core.body;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class is a Map between UniqueID and either remote or local bodies.
 * It accepts event listeners interested in BodyEvent.
 * Body event are produced whenever a body is added or removed from
 * the collection.
 * </p><p>
 * In case of serialization of a object of this class, all reference to local bodies will
 * get serialized as reference of remote body. Local bodies are never serialized from
 * this container.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.1,  2001/12/23
 * @since   ProActive 0.9
 */
public class BodyMap /* extends AbstractEventProducer */ implements Cloneable, java.io.Externalizable {
    //
    // -- PRIVATE MEMBER -----------------------------------------------
    //
    private Hashtable<UniqueID, UniversalBody> idToBodyMap;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public BodyMap() {
        idToBodyMap = new Hashtable<UniqueID, UniversalBody>();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    // 

    /**
     * add the set (id, node) in the idToBodyMap
     * block if it already exists until it is removed
     */
    public synchronized void putBody(UniqueID id, UniversalBody b) {
        while (idToBodyMap.get(id) != null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        idToBodyMap.put(id, b);

        // ProActiveEvent
        //        if (hasListeners()) {
        //            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CREATED));
        //        }

        // END ProActiveEvent
    }

    /**
     * add the set (id, node) in the idToBodyMap
     * erase any previous entry
     */
    public synchronized void updateBody(UniqueID id, UniversalBody b) {
        //remove old reference
        if (idToBodyMap.get(id) != null) {
            idToBodyMap.remove(id);
        }

        //add new reference
        idToBodyMap.put(id, b);

        // ProActiveEvent
        //        if (hasListeners()) {
        //            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CREATED));
        //        }

        // END ProActiveEvent
    }

    public synchronized void removeBody(UniqueID id) {
        /* UniversalBody b = */idToBodyMap.remove(id);
        notifyAll();

        // ProActiveEvent
        //        if ((b != null) && hasListeners()) {
        //            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_DESTROYED));
        //        }

        // END ProActiveEvent
    }

    public/* synchronized */int size() { // The java.util.Hashtable#size() method is already synchronized
        return idToBodyMap.size();
    }

    public synchronized UniversalBody getBody(UniqueID id) {
        Object o = null;
        if (id != null) {
            o = idToBodyMap.get(id);
        }

        return (UniversalBody) o;
    }

    public synchronized boolean containsBody(UniqueID id) {
        return idToBodyMap.containsKey(id);
    }

    public java.util.Iterator<UniversalBody> bodiesIterator() {
        return idToBodyMap.values().iterator();
    }

    @Override
    public synchronized String toString() {
        String ls = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        sb.append("-- BodyMap -------").append(ls);
        for (Map.Entry<UniqueID, UniversalBody> entry : idToBodyMap.entrySet()) {
            sb.append(entry.getKey()).append("  body = ").append(entry.getValue()).append(ls);
        }
        return sb.toString();
    }

    //
    // -- implements Cloneable -----------------------------------------------
    //
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        BodyMap newLocationTable = new BodyMap();

        newLocationTable.idToBodyMap = (Hashtable<UniqueID, UniversalBody>) idToBodyMap.clone();

        return newLocationTable;
    }

    //
    // -- methods for BodyEventProducer -----------------------------------------------
    //    //
    //    public void addBodyEventListener(BodyEventListener listener) {
    //        addListener(listener);
    //    }
    //
    //    public void removeBodyEventListener(BodyEventListener listener) {
    //        removeListener(listener);
    //    }

    //
    // -- implements Externalizable -----------------------------------------------
    //

    /**
     * The object implements the readExternal method to restore its contents by calling the methods
     * of DataInput for primitive types and readObject for objects, strings and arrays.
     */
    public synchronized void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
        int size = in.readInt();

        for (int i = 0; i < size; i++) {
            UniqueID id = (UniqueID) in.readObject();
            UniversalBody remoteBody = (UniversalBody) in.readObject();
            idToBodyMap.put(id, remoteBody);
        }
    }

    /**
     * The object implements the writeExternal method to save its contents by calling the methods
     * of DataOutput for its primitive values or calling the writeObject method of ObjectOutput
     * for objects, strings, and arrays.
     */
    public synchronized void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        int size = idToBodyMap.size();
        out.writeInt(size);

        Set<Map.Entry<UniqueID, UniversalBody>> entrySet = idToBodyMap.entrySet();
        java.util.Iterator<Map.Entry<UniqueID, UniversalBody>> iterator = entrySet.iterator();

        while (iterator.hasNext()) {
            Map.Entry<UniqueID, UniversalBody> entry = iterator.next();
            out.writeObject(entry.getKey());

            Object value = entry.getValue();

            if (value instanceof Body) {
                out.writeObject(((Body) value).getRemoteAdapter());
            } else {
                out.writeObject(value);
            }
        }
    }
}
