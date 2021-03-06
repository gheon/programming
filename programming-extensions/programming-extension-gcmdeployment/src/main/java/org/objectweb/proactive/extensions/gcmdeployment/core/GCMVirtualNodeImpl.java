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
package org.objectweb.proactive.extensions.gcmdeployment.core;

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;
import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCM_NODEMAPPER_LOGGER;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.TechnicalServicesFactory;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.TechnicalServicesProperties;
import org.objectweb.proactive.gcmdeployment.Topology;
import org.objectweb.proactive.utils.TimeoutAccounter;


public class GCMVirtualNodeImpl implements GCMVirtualNodeInternal {
    static final private Map<UniqueID, GCMVirtualNodeInternal> localVirtualNodes = new ConcurrentHashMap<UniqueID, GCMVirtualNodeInternal>();

    static final public GCMVirtualNodeImpl DEFAULT_VN;

    static {
        DEFAULT_VN = new GCMVirtualNodeImpl();
        DEFAULT_VN.setName("DefaultVN");
    }

    /**
     * unique name (declared by GCMA)
     */
    private String id;

    private UniqueID uniqueID;

    /**
     * capacity (declared by GCMA)
     */
    private long capacity;

    /**
     * All Node Provider Contracts (declared by GCMA)
     */
    final private Set<NodeProviderContract> nodeProvidersContracts;

    /**
     * All the Nodes attached to this VN
     */
    final private List<Node> nodes;

    final private Object isReadyMonitor = new Object();

    private int getANewNodeIndex = 0;

    private List<Node> previousNodes;

    final private Set<Subscriber> nodeAttachmentSubscribers;

    final private Set<Subscriber> isReadySubscribers;

    private TopologyRootImpl deploymentTree;

    private TechnicalServicesProperties nodeTechnicalServicesProperties;

    private TechnicalServicesProperties applicationTechnicalServicesProperties;

    private boolean readyNotifSent = false;

    static public GCMVirtualNodeInternal getLocal(UniqueID uniqueID) {
        return localVirtualNodes.get(uniqueID);
    }

    public GCMVirtualNodeImpl() {
        this(TechnicalServicesProperties.EMPTY);
    }

    public GCMVirtualNodeImpl(TechnicalServicesProperties applicationTechnicalServicesProperties) {
        this.uniqueID = new UniqueID();
        GCMVirtualNodeImpl.localVirtualNodes.put(uniqueID, this);

        this.applicationTechnicalServicesProperties = applicationTechnicalServicesProperties;
        nodeProvidersContracts = new HashSet<NodeProviderContract>();
        nodes = new LinkedList<Node>();

        nodeAttachmentSubscribers = new HashSet<Subscriber>();
        isReadySubscribers = new HashSet<Subscriber>();
        nodeTechnicalServicesProperties = TechnicalServicesProperties.EMPTY;
    }

    /*
     * ------------------- GCMVirtualNode interface
     */
    public String getName() {
        return id;
    }

    public boolean isGreedy() {
        return capacity == MAX_CAPACITY;
    }

    public boolean isReady() {
        synchronized (nodes) {
            return (!hasUnsatisfiedContract()) && (!needNode());
        }
    }

    public long getNbRequiredNodes() {
        long acc = 0;
        for (NodeProviderContract contract : nodeProvidersContracts) {
            if (!contract.isGreedy()) {
                acc += contract.capacity;
            }
        }

        long ret = Math.max(Math.max(capacity, acc), 0);
        return ret;
    }

    public long getNbCurrentNodes() {
        synchronized (nodes) {
            return nodes.size();
        }
    }

    public List<Node> getCurrentNodes() {
        synchronized (nodes) {
            return new ArrayList<Node>(nodes);
        }
    }

    public void waitReady() {
        try {
            waitReady(0);
        } catch (ProActiveTimeoutException e) {
            // unreachable, 0 means no timeout
            GCM_NODEMAPPER_LOGGER.error("Unreachable code !", e);
        }

    }

    public void waitReady(long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        while (!time.isTimeoutElapsed()) {
            synchronized (isReadyMonitor) {
                try {
                    if (isReady())
                        return;
                    isReadyMonitor.wait(time.getRemainingTimeout());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        // Provide some information to help the user to understand why the timeout is reached
        StringBuilder sb = new StringBuilder();
        sb.append("waitReady timeout reached on " + this.getName() + ", some debug information follow:\n");
        if (needNode()) {
            sb.append("\t Capacity requirements are not fullfilled: " + capacity + " nodes required but only " +
                      getNbCurrentNodes() + "mapped\n");
        }
        if (hasUnsatisfiedContract()) {
            sb.append("\t Some contract are not satisfied:\n");
            for (NodeProviderContract nodeProviderContract : nodeProvidersContracts) {
                if (nodeProviderContract.needNode()) {
                    String id = nodeProviderContract.nodeProvider.getId();
                    long capacity = nodeProviderContract.capacity;
                    long mapped = nodeProviderContract.nodes;
                    sb.append("\t\t " + id + " Capacity=" + capacity + "but only " + mapped + " nodes mapped\n");
                }
            }
        }
        throw new ProActiveTimeoutException(sb.toString());
    }

    public Node getANode() {
        return getANode(0);
    }

    public Node getANode(int timeout) {

        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        while (!time.isTimeoutElapsed()) {
            synchronized (nodes) {
                try {
                    if (nodes.size() > getANewNodeIndex) {
                        Node node = nodes.get(getANewNodeIndex);
                        getANewNodeIndex++;
                        return node;
                    }
                    nodes.wait(time.getRemainingTimeout());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public List<Node> getNewNodes() {
        List<Node> nodesCopied;
        synchronized (nodes) {
            nodesCopied = new ArrayList<Node>(nodes);
        }
        if (previousNodes == null) {
            previousNodes = new LinkedList<Node>(nodesCopied);
        } else {
            nodesCopied.removeAll(previousNodes);
            previousNodes.addAll(nodesCopied);
        }

        return nodesCopied;
    }

    public void subscribeNodeAttachment(Object client, String methodeName, boolean withHistory)
            throws ProActiveException {
        if ((client == null) || (methodeName == null)) {
            throw new ProActiveException("Client and MethodName cannot be null");
        }

        Class<?> cl = client.getClass();
        try {
            Method m = cl.getMethod(methodeName, Node.class, String.class);

            if (withHistory) {
                List<Node> copyOfNodes;
                synchronized (this.nodes) {
                    copyOfNodes = new ArrayList<Node>(nodes);
                    synchronized (nodeAttachmentSubscribers) {
                        nodeAttachmentSubscribers.add(new Subscriber(client, methodeName));
                    }
                }

                for (Node node : copyOfNodes) {
                    try {
                        m.invoke(client, node, this.getName());
                    } catch (Throwable e) {
                        GCM_NODEMAPPER_LOGGER.warn("Notification on node attachement failed", e);
                    }
                }
            } else {
                synchronized (nodeAttachmentSubscribers) {
                    nodeAttachmentSubscribers.add(new Subscriber(client, methodeName));
                }
            }
        } catch (NoSuchMethodException e) {
            GCM_NODEMAPPER_LOGGER.warn("Method " + methodeName + "(Node, String) cannot be found on " +
                                       cl.getSimpleName());
            throw new ProActiveException("Method " + methodeName + "(Node, String) cannot be found on " +
                                         cl.getSimpleName(), e);
        }
    }

    public void unsubscribeNodeAttachment(Object client, String methodeName) throws ProActiveException {
        boolean success;

        synchronized (nodeAttachmentSubscribers) {
            success = nodeAttachmentSubscribers.remove(new Subscriber(client, methodeName));
        }

        if (!success) {
            throw new ProActiveException("No such listener found");
        }
    }

    public void subscribeIsReady(Object client, String methodeName) throws ProActiveException {
        if (isGreedy()) {
            throw new ProActiveException("subscribeIsReady is not applicable with greedy Virtual Node");
        }

        if ((client == null) || (methodeName == null)) {
            throw new ProActiveException("Client and MethodName cannot be null");
        }

        Class<?> cl = client.getClass();
        try {
            cl.getMethod(methodeName, String.class);
            synchronized (isReadySubscribers) {
                isReadySubscribers.add(new Subscriber(client, methodeName));
            }
        } catch (NoSuchMethodException e) {
            throw new ProActiveException("Method " + methodeName + "(GCMVirtualNode) cannot be found on " +
                                         cl.getSimpleName(), e);
        }
    }

    public void unsubscribeIsReady(Object client, String methodeName) throws ProActiveException {
        boolean success;
        synchronized (isReadySubscribers) {
            success = isReadySubscribers.remove(new Subscriber(client, methodeName));
        }

        if (!success) {
            throw new ProActiveException("No such listener found");
        }
    }

    public Topology getCurrentTopology() {
        Set<Node> nodesCopied;
        synchronized (nodes) {
            nodesCopied = new HashSet<Node>(nodes);
        }
        return TopologyImpl.createTopology(deploymentTree, nodesCopied);
    }

    public void updateTopology(Topology topology) {
        Set<Node> nodesCopied;
        synchronized (nodes) {
            nodesCopied = new HashSet<Node>(nodes);
        }
        TopologyImpl.updateTopology(topology, nodesCopied);
    }

    /*
     * ------------------- GCMVirtualNodeInternal interface
     */
    public void addNodeProviderContract(NodeProvider provider, TechnicalServicesProperties techServProperties,
            long capacity) {
        if (findNodeProviderContract(provider) != null) {
            throw new IllegalStateException("A contract with the provider " + provider.getId() + " already exist for " +
                                            id);
        }

        nodeProvidersContracts.add(new NodeProviderContract(provider, techServProperties, capacity));

        if (this.capacity > 0) {
            int acc = 0;
            for (NodeProviderContract npc : nodeProvidersContracts) {
                acc += npc.capacity;
            }

            if (acc > this.capacity) {
                GCMA_LOGGER.warn("Virtual Node " + id + " capacity is lower than Node Provider contracts requirements");
            }
        }
    }

    public boolean doesNodeProviderNeed(FakeNode fakeNode, NodeProvider nodeProvider) {
        NodeProviderContract contract = findNodeProviderContract(nodeProvider);

        if ((contract != null) && contract.doYouNeed(fakeNode, nodeProvider) && (needNode() || isGreedy())) {
            addNode(fakeNode);
            return true;
        }

        return false;
    }

    public boolean doYouNeed(FakeNode fakeNode, NodeProvider nodeProvider) {
        if (!needNode()) {
            return false;
        }

        NodeProviderContract contract = findNodeProviderContract(nodeProvider);
        if ((contract != null) && contract.isGreedy()) {
            addNode(fakeNode, contract);
            return true;
        }

        return false;
    }

    public boolean doYouWant(FakeNode fakeNode, NodeProvider nodeProvider) {
        if (!isGreedy()) {
            return false;
        }

        NodeProviderContract contract = findNodeProviderContract(nodeProvider);
        if ((contract != null) && contract.isGreedy()) {
            addNode(fakeNode);
            return true;
        }

        return false;
    }

    public boolean hasContractWith(NodeProvider nodeProvider) {
        return null != findNodeProviderContract(nodeProvider);
    }

    public boolean hasUnsatisfiedContract() {
        for (NodeProviderContract nodeProviderContract : nodeProvidersContracts) {
            if (nodeProviderContract.needNode()) {
                return true;
            }
        }
        return false;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public void setName(String id) {
        this.id = id;
    }

    public void setDeploymentTree(TopologyRootImpl deploymentTree) {
        this.deploymentTree = deploymentTree;
    }

    public void addNode(FakeNode fakeNode) {
        addNode(fakeNode, null);
    }

    public void addNode(FakeNode fakeNode, NodeProviderContract contract) {
        GCM_NODEMAPPER_LOGGER.debug("One Node " + fakeNode.getRuntimeURL() + " attached to " + getName());
        Node node;
        synchronized (nodes) {
            TechnicalServicesProperties tsProperties = applicationTechnicalServicesProperties.getCombinationWith(nodeTechnicalServicesProperties);

            if (contract != null) {
                tsProperties = tsProperties.getCombinationWith(contract.getTechnicalServicesProperties());
            }

            List<TechnicalService> tsList = new ArrayList<TechnicalService>();

            for (Map.Entry<String, HashMap<String, String>> tsp : tsProperties) {

                TechnicalService ts = TechnicalServicesFactory.create(tsp.getKey(), tsp.getValue());
                if (ts != null) {
                    tsList.add(ts);
                }
            }

            try {
                node = fakeNode.create(this, tsList);

                // If node creation failed, then following code blocks are NOT reached
                // and the node is not added to this virtual node.

                if (contract != null) {
                    contract.addNode(node);
                }

                nodes.add(node);
                nodes.notifyAll();

                synchronized (nodeAttachmentSubscribers) {
                    for (Subscriber subscriber : nodeAttachmentSubscribers) {
                        Class<?> cl = subscriber.getClient().getClass();
                        try {
                            Method m = cl.getMethod(subscriber.getMethod(), Node.class, String.class);
                            m.invoke(subscriber.getClient(), node, this.getName());
                        } catch (Exception e) {
                            GCM_NODEMAPPER_LOGGER.warn("Notification on node attachement failed", e);
                        }
                    }
                }
            } catch (NodeException e) {
                GCMA_LOGGER.warn("GCM Deployment failed to create a node on " + fakeNode.getRuntimeURL() +
                                 ". Please check your network configuration", e);
            }
        }

        if (isReady() && !readyNotifSent) {
            synchronized (isReadyMonitor) {
                isReadyMonitor.notifyAll();
            }

            synchronized (isReadySubscribers) {
                for (Subscriber subscriber : isReadySubscribers) {
                    Class<?> cl = subscriber.getClient().getClass();
                    try {
                        Method m = cl.getMethod(subscriber.getMethod(), String.class);
                        m.invoke(subscriber.getClient(), this.getName());
                        isReadySubscribers.remove(subscriber);
                    } catch (Exception e) {
                        GCM_NODEMAPPER_LOGGER.warn(e);
                    }
                }
            }
            readyNotifSent = true;
        }
    }

    /*
     * ------------------- Private Helpers
     */

    private NodeProviderContract findNodeProviderContract(NodeProvider nodeProvider) {
        for (NodeProviderContract nodeProviderContract : nodeProvidersContracts) {
            if (nodeProvider == nodeProviderContract.getNodeProvider()) {
                return nodeProviderContract;
            }
        }

        return null;
    }

    private boolean needNode() {
        synchronized (nodes) {
            return !isGreedy() && (nodes.size() < capacity);
        }
    }

    private boolean wantNode() {
        return needNode() || isGreedy();
    }

    static class NodeProviderContract {
        NodeProvider nodeProvider;

        TechnicalServicesProperties technicalServicesProperties;

        long capacity;

        long nodes;

        NodeProviderContract(NodeProvider nodeProvider,
                TechnicalServicesProperties associatedTechnicalServicesProperties, long capacity) {
            this.nodeProvider = nodeProvider;
            this.technicalServicesProperties = associatedTechnicalServicesProperties;
            this.capacity = capacity;
            this.nodes = 0;
        }

        public boolean doYouNeed(FakeNode fakeNode, NodeProvider nodeProvider) {
            if (this.nodeProvider != nodeProvider) {
                return false;
            }

            if (isGreedy()) {
                return false;
            }

            if (nodes >= capacity) {
                return false;
            }

            nodes++;
            return true;
        }

        public boolean isGreedy() {
            return capacity == MAX_CAPACITY;
        }

        public boolean needNode() {
            return !isGreedy() && (this.nodes < this.capacity);
        }

        public NodeProvider getNodeProvider() {
            return nodeProvider;
        }

        public void addNode(Node node) {
            nodes++;
        }

        public TechnicalServicesProperties getTechnicalServicesProperties() {
            return technicalServicesProperties;
        }

    }

    static private class Subscriber {
        private Object client;

        private String method;

        public Subscriber(Object client, String method) {
            this.client = client;
            this.method = method;
        }

        public Object getClient() {
            return client;
        }

        public void setClient(Object client) {
            this.client = client;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((client == null) ? 0 : client.hashCode());
            result = (prime * result) + ((method == null) ? 0 : method.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Subscriber other = (Subscriber) obj;
            if (client == null) {
                if (other.client != null) {
                    return false;
                }
            } else if (!client.equals(other.client)) {
                return false;
            }
            if (method == null) {
                if (other.method != null) {
                    return false;
                }
            } else if (!method.equals(other.method)) {
                return false;
            }
            return true;
        }
    }

    public void setTechnicalServicesProperties(TechnicalServicesProperties technicalServices) {
        this.nodeTechnicalServicesProperties = technicalServices;
    }

    public void addTechnicalServiceProperties(TechnicalServicesProperties technicalServices) {
        this.nodeTechnicalServicesProperties = this.nodeTechnicalServicesProperties.getCombinationWith(technicalServices);
    }

    public UniqueID getUniqueID() {
        return uniqueID;
    }

}
