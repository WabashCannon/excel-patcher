package format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DependencyDigraph {
	/** Set of all the nodes in the graph */
	Set<Node> nodes = new HashSet<Node>();
	
	/**
	 * Adds a childNode with the given name to the parent node
	 * if it exists. Otherwise, creates it and adds it.
	 * 
	 * @param parentName
	 * @param childName
	 */
	public void addChildToParent(String parentName, String childName){
		Node parent = sureGetNodeByName(parentName);
		Node child = sureGetNodeByName(childName);
		
		parent.addChild(child);
		child.addParent(parent);
	}
	
	/**
	 * Returns the node with the given name if it is in the digraph.
	 * Otherwise it returns null.
	 * 
	 * @param nodeName to get
	 * @return node if it is in the digraph, otherwise null
	 */
	private Node getNodeByName(String nodeName){
		Iterator<Node> iterator = nodes.iterator();
		while( iterator.hasNext() ){
			Node node = iterator.next();
			if ( node.getName().equals(nodeName) ){
				return node;
			}
		}
		return null;
	}
	
	/**
	 * Returns the node with the given name if it exists in the digraph,
	 * otherwise, creates it, and returns it.
	 * 
	 * @param nodeName of the node to get
	 * @return the node with the given name
	 */
	private Node sureGetNodeByName(String nodeName){
		Node node = getNodeByName(nodeName);
		if ( node == null ){
			node = createNode(nodeName);
		}
		return node;
	}
	
	/**
	 * Creates a node with the given name and adds it to the digraph
	 * 
	 * @param nodeName for the new node
	 * @return then new node
	 */
	private Node createNode(String nodeName){
		Node node = new Node(nodeName);
		nodes.add(node);
		return node;
	}
	
	public Set<String> getLeaves(){
		Set<String> leaves = new HashSet<String>();
		
		for ( Node node : nodes ){
			if ( node.children.size() == 0 ){
				leaves.add(node.getName());
			}
		}
		
		return leaves;
	}
	
	//###########################################################
	//##### Node subclass
	//###########################################################
	/**
	 * This class contains the nodes of the dependency digraph.
	 * 
	 * @author Ashton Dyer (WabashCannon)
	 *
	 */
	private class Node implements Comparable<Node> {
		/** This node's name */
		String name;
		/** This node's parents */
		ArrayList<Node> parents = new ArrayList<Node>();
		/** This node's children */
		ArrayList<Node> children = new ArrayList<Node>();
		
		/**
		 * Creates a new node with the given name.
		 * 
		 * @param name of the new node
		 */
		private Node(String name){
			this.name = name;
		}
		
		/**
		 * Adds a parent to the node if it is not already a child. Returns
		 * if it was added.
		 * 
		 * @param parent node to add
		 * @return if the parent was added
		 */
		private boolean addParent(Node parent){
			if ( !parents.contains(parent) ){
				parents.add(parent);
				Collections.sort(parents);
				return true;
			}
			return false;
		}
		
		/**
		 * Removes a parent from the node if it exists.
		 * 
		 * @param parent to remove from children
		 * @return if the node was removed
		 */
		private boolean removeParent(Node parent){
			return parents.remove(parent);
		}
		
		/**
		 * Adds a child to the node if it is not already a child. Returns
		 * if it was added.
		 * 
		 * @param child node to add
		 * @return if the child was added
		 */
		private boolean addChild(Node child){
			if ( !children.contains(child) ){
				children.add(child);
				Collections.sort(children);
				return true;
			}
			return false;
		}
		
		/**
		 * Removes a child from the node if it exists.
		 * 
		 * @param child to remove from children
		 * @return if the node was removed
		 */
		private boolean removeChild(Node child){
			return children.remove(child);
		}
		
		/**
		 * Returns this node's name
		 * @return this node's name
		 */
		private String getName(){
			return name;
		}
		
		@Override
		public int compareTo(Node otherNode) {
			return getName().compareTo(otherNode.getName());
		}
	}
}
