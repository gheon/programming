package org.objectweb.proactive.core.component;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.core.component.type.ProActiveComponentType;

import java.io.Serializable;

import java.util.Vector;


/** Contains the configuration of a component :
 * - type
 * - interfaces (server and client) --> in contained ControllerDescription object
 * - name --> in contained ControllerDescription object
 * - hierarchical type (primitive or composite) --> in contained ControllerDescription object
 * - a ref on the stub on the base object
 */
public class ComponentParameters implements Serializable {
    protected static Logger logger = Logger.getLogger(ComponentParameters.class.getName());

    private Object stubOnReifiedObject;
    private ComponentType componentType;
    private ControllerDescription controllerDesc;

    /** Constructor for ComponentParameters.
     * @param name the name of the component
     * @param hierarchicalType the hierarchical type, either PRIMITIVE or COMPOSITE or PARALLEL
     * @param componentType
     */
    public ComponentParameters(String name, String hierarchicalType,
        ComponentType componentType) {
        this(componentType, new ControllerDescription(name, hierarchicalType));
    }

    /**
     * Constructor
     * @param componentType the type of the component
     * @param controllerDesc a ControllerDescription object
     */
    public ComponentParameters(ComponentType componentType,
        ControllerDescription controllerDesc) {
        this.componentType = componentType;
        this.controllerDesc = controllerDesc;
    }

    /**
     * overrides the clone method of Object
     * @return a clone of this current object
     */
    public Object clone() {
        return new ComponentParameters(new ProActiveComponentType(componentType),
            new ControllerDescription(controllerDesc));
    }

    /**
     * setter for the name
     * @param name name of the component
     */
    public void setName(String name) {
        controllerDesc.setName(name);
    }

    /**
     * Returns the componentType.
     * @return ComponentType
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    /**
     * getter
     * @return a ControllerDescription object
     */
    public ControllerDescription getControllerDescription() {
        return controllerDesc;
    }

    /**
     * setter
     * @param componentType the type of the component
     */
    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    /**
     * setter
     * @param string the hierarchical type (primitive, composite or parallel)
     */
    public void setHierarchicalType(String string) {
        controllerDesc.setHierarchicalType(string);
    }

    /**
     * getter
     * @return the name
     */
    public String getName() {
        return controllerDesc.getName();
    }

    /**
     * Returns the hierarchicalType.
     * @return String
     */
    public String getHierarchicalType() {
        return controllerDesc.getHierarchicalType();
    }

    /**
     * @return the types of server interfaces
     */
    public InterfaceType[] getServerInterfaceTypes() {
        Vector server_interfaces = new Vector();
        InterfaceType[] interfaceTypes = componentType.getFcInterfaceTypes();
        for (int i = 0; i < interfaceTypes.length; i++) {
            if (!interfaceTypes[i].isFcClientItf()) {
                server_interfaces.add(interfaceTypes[i]);
            }
        }
        return (InterfaceType[]) server_interfaces.toArray(new InterfaceType[server_interfaces.size()]);
    }

    /**
     * @return the types of client interfacess
     */
    public InterfaceType[] getClientInterfaceTypes() {
        Vector client_interfaces = new Vector();
        InterfaceType[] interfaceTypes = componentType.getFcInterfaceTypes();
        for (int i = 0; i < interfaceTypes.length; i++) {
            if (interfaceTypes[i].isFcClientItf()) {
                client_interfaces.add(interfaceTypes[i]);
            }
        }
        return (InterfaceType[]) client_interfaces.toArray(new InterfaceType[client_interfaces.size()]);
    }

    /**
     * accessor on the standard ProActive stub
     * @return standard ProActive stub on the reified object
     */
    public Object getStubOnReifiedObject() {
        return stubOnReifiedObject;
    }

    /**
     * keeps a reference on the standard ProActive stub
     * @param ref on an instance of a standard ProActive stub on the reified object
     */
    public void setStubOnReifiedObject(Object object) {
        stubOnReifiedObject = object;
    }

    /**
     * getter
     * @return a table of interface types
     */
    public InterfaceType[] getInterfaceTypes() {
        return componentType.getFcInterfaceTypes();
    }
}
