/*
 * Created on Mar 16, 2004
 */
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * This class represents a group by a two-dimensional topology.
 *
 * @author Laurent Baduel
 */
public class Plan extends Line { // implements Topology2D {

    /** height of the two-dimensional topology group */
    protected int height; //  => Y => number of lines

    /**
     * Construtor. The members of <code>g</code> are used to fill the topology group.
     * @param g - the group used a base for the new group (topology)
     * @param height - the heigth of the two-dimensional topology group
     * @param width - the width of the two-dimensional topology group
     * @throws ConstructionOfReifiedObjectFailedException
     */
    public Plan(Group g, int height, int width)
        throws ConstructionOfReifiedObjectFailedException {
        super(g, height * width);
        this.height = height;
        this.width = width;
    }

    /**
     * Construtor. The members of <code>g</code> are used to fill the topology group.
     * @param g - the group used a base for the new group (topology)
     * @param nbMembers - the number of member in the two-dimensional topology group
     * @throws ConstructionOfReifiedObjectFailedException
     */
    protected Plan(Group g, int nbMembers)
        throws ConstructionOfReifiedObjectFailedException {
        super(g, nbMembers);
    }

    /**
     * Return the size of the lines
     * @return the size of the the lines
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height of the two-dimensional topology group (number of lines)
     * @return the height of the two-dimensional topology group
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the coordonate X for the specified position, according to the dimension
     * @param position
     * @return the coordonate X
     */
    private int getX(int position) {
        return position % this.width;
    }

    /**
     * Returns the coordonate Y for the specified position, according to the dimension
     * @param position
     * @return the coordonate Y
     */
    private int getY(int position) {
        return position % this.height;
    }

    /**
     * Returns the object at the left of the specified object in the two-dimensional topology group
     * @param o - the specified object
     * @return the object at the left of <code>o<code>. If there is no object at the left of <code>o</code>, return <code>null</code>
     */
    public Object left(Object o) {
        int pos = this.indexOf(o);
        if ((pos % this.getWidth()) == 0) {
            return null;
        } else {
            return this.get(pos - 1);
        }
    }

    /**
     * Returns the X-coordonate of the specified object
     * @param o - the object
     * @return the position (in X) of the object in the Plan
     */
    public int getX(Object o) {
        return this.indexOf(o) % this.getWidth();
    }

    /**
     * Returns the Y-coordonate of the specified object
     * @param o - the object
     * @return the position (in Y) of the object in the Plan
     */
    public int getY(Object o) {
        return this.indexOf(o) / this.getWidth();
    }

    /**
     * Returns the object at the right of the specified object in the two-dimensional topology group
     * @param o - the specified object
     * @return the object at the right of <code>o<code>. If there is no object at the right of <code>o</code>, return <code>null</code>
     */
    public Object right(Object o) {
        int pos = this.indexOf(o);
        if ((pos % this.getWidth()) == (this.getWidth() - 1)) {
            return null;
        } else {
            return this.get(pos + 1);
        }
    }

    /**
     * Returns the object at the up of the specified object in the two-dimensional topology group
     * @param o - the specified object
     * @return the object at the up of <code>o<code>. If there is no object at the up of <code>o</code>, return <code>null</code>
     */
    public Object up(Object o) {
        int pos = this.indexOf(o);
        if (pos < this.getWidth()) {
            return null;
        } else {
            return this.get(pos - this.getWidth());
        }
    }

    /**
     * Returns the object at the down of the specified object in the two-dimensional topology group
     * @param o - the specified object
     * @return the object at the down of <code>o<code>. If there is no object at the down of <code>o</code>, return <code>null</code>
     */
    public Object down(Object o) {
        int pos = this.indexOf(o);
        if (pos > (((this.getHeight() - 1) * this.getWidth()) - 1)) {
            return null;
        } else {
            return this.get(pos + this.getWidth());
        }
    }

    /**
     * Returns the line (one-dimensional topology group) with the specified number
     * @param line - the number of the line
     * @return the one-dimensional topology group formed by the line in the two-dimensional topology group, return <code>null</code> if the the specified line is incorrect
     */
    public Line line(int line) {
        if ((line < 0) || (line > this.getWidth())) {
            return null;
        }
        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        int begining = line * this.getWidth();
        for (int i = begining; i < (begining + this.getWidth()); i++) {
            tmp.add(this.get(i));
        }
        Line result = null;
        try {
            result = new Line((Group) tmp, this.getWidth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the line that contains the specified object
     * @param o - the object
     * @return the one-dimensional topology group formed by the line of the object in the two-dimensional topology group
     */
    public Line line(Object o) {
        return this.line(this.getY(this.indexOf(o)));
    }

    /**
     * Returns the column (one-dimensional topology group) with the specified number
     * @param column - the number of the line
     * @return the one-dimensional topology group formed by the column in the two-dimensional topology group, return <code>null</code> if the the specified line is incorrect
     */
    public Line column(int column) {
        if ((column < 0) || (column > this.getHeight())) {
            return null;
        }
        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        int begining = column;
        for (int i = 0; i < this.getHeight(); i++) {
            tmp.add(this.get(column + (i * this.getWidth())));
        }
        Line result = null;
        try {
            result = new Line((Group) tmp, this.getWidth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the column that contains the specified object
     * @param o - the object
     * @return the one-dimensional topology group formed by the column of the object in the two-dimensional topology group
     */
    public Line column(Object o) {
        return this.column(this.getX(this.indexOf(o)));
    }
}
