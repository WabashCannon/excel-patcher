package format;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.Logger;

/**
 * A data structure designed to be used when determining the dependencies
 * of column's format data.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class DependencyTree {
	/** The root node in the tree */
    private Node root;
    
    /**
     * Creates a new dependency tree with the given root name.
     * 
     * @param rootName name of the root node in the tree
     */
    public DependencyTree(String rootName) {
        root = new Node();
        root.name = rootName;
        root.children = new ArrayList<Node>();
    }
    
    /**
     * Adds a set of new child nodes to the specified child node
     * 
     * @param childToAddTo
     * @param newChildren
     * @return if any children were added
     */
    public boolean addToChild(String childToAddTo, Set<String> newChildren){
    	Node nodeToAddTo = findChildNode(childToAddTo, root);
    	assert( nodeToAddTo != null );
    	if ( nodeToAddTo.children == null ){
    		nodeToAddTo.children = new ArrayList<Node>();
    	}
    	//If at least one child was added
    	boolean addedAChild = false;
    	for ( String newChildName : newChildren ){
    		if ( newChildName.equals(root.name) ){
    			Logger.log("Error", "Cyclic dependency found");
    		}
    		if ( findChildNode(newChildName, root) == null ){
    			Node newChild = new Node();
    			newChild.name = newChildName;
    			nodeToAddTo.children.add( newChild );
    			addedAChild = true;
    		} else {
    			nodeToAddTo.children.add( findChildNode(newChildName, root) );
    		}
    	}
    	return addedAChild;
    }
    
    /**
     * Tries to find a child node with the given name. Returns the node
     * if successful, and null otherwise.
     * 
     * @param name of the child node to search for
     * @param parent node to search
     * @return the child node if found, null otherwise
     */
    public Node findChildNode(String name, Node parent){
    	//If it is this node, we're done, should only really happen when it's root
    	if ( parent.name.equals(name) ){
    		return parent;
    	}
    	//If it is a leaf, it doesn't have the child
    	if ( parent.isLeaf() ){
    		return null;
    	}
    	//Check surface children
    	for ( Node child : parent.children ){
    		if ( child.name.equals(name) ){
    			return child;
    		}
    	}
    	//Check one layer down (recursively)
    	for ( Node child : parent.children ){
    		Node child2 = findChildNode(name, child);
    		if ( child2 != null && child2.name.equals(name) ){
    			return child2;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Returns the set of leaves in the tree
     * @return
     */
    public Set<String> getLeaves(){
    	Set<String> leaves = root.getLeaves();
    	if ( leaves.contains(root.name) ){
    		leaves.remove(root.name);
    	}
    	return leaves;
    }
    
    @Override
    public String toString(){
    	return root.toString(root.name.length());
    }
    
    /**
     * Node object used to build dependency tree
     * 
     * @author Ashton Dyer (WabashCannon)
     *
     */
    public static class Node {
    	/** Name of the node */
        private String name;
        /** The node's parent */
        private Node parent;
        /** The children of this node */
        private List<Node> children;
        
        /**
         * Returns if this node is a leaf node
         * 
         * @return if this node is a leaf node
         */
        public boolean isLeaf(){
        	return children == null || children.size() == 0;
        }
        
        /**
         * Returns the set of leaves branching from this node
         * 
         * @return the set of leaves branching from this node
         */
        public Set<String> getLeaves(){
        	Set<String> leaves = new HashSet<String>();
        	if ( isLeaf() ){
        		leaves.add(name);
        		return leaves;
        	} else {
        		for ( Node child : children ){
        			leaves.addAll( child.getLeaves() );
        		}
        		return leaves;
        	}
        }
        
        /**
         * Utility method for cleanly printing trees.
         * 
         * @param depth of this node in the tree
         * @return a string representation of this node
         */
        public String toString(int depth){
        	if ( isLeaf() ){return name+"\n";}
        	String str = name;
        	boolean isFirst = true;
        	for ( Node child : children ){
        		if ( !isFirst ){
		        	for ( int i = 0 ; i < depth ; i++ ){
		        		str += " ";
		        	}
        		} else {
        			isFirst = false;
        		}
	        	str += " -> "+child.toString(depth+name.length()+4);
        	}
        	return str;
        }
        
        /**
         * Returns this node's parent
         * 
         * @return this node's parent
         */
		public Node getParent() {
			return parent;
		}
		
		/**
		 * Sets this node's parent
		 * 
		 * @param newParent to set
		 */
		public void setParent(Node newParent) {
			this.parent = newParent;
		}
    }
}
