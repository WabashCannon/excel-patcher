package format;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.Logger;

public class DependencyTree {
    private Node root;

    public DependencyTree(String rootName) {
        root = new Node();
        root.name = rootName;
        root.children = new ArrayList<Node>();
    }
    
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
    
    public Set<String> getLeaves(){
    	Set<String> leaves = root.getLeaves();
    	if ( leaves.contains(root.name) ){
    		leaves.remove(root.name);
    	}
    	return leaves;
    }
    
    public String toString(){
    	return root.toString(root.name.length());
    }

    public static class Node {
        private String name;
        private Node parent;
        private List<Node> children;
        
        public boolean isLeaf(){
        	return children == null || children.size() == 0;
        }
        
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

		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}
    }
}

/*

public class DependencyTree {
	String name;
	List<String> children;
	
	public DependencyTree(String name, List<String> children){
		assert( name != null && children != null );
		this.name = name;
		this.children = children;
	}
	
	public void addToChild(String childName, List<String> toAdd){
		assert( childName != null && toAdd != null );
		for ( String newChildName : toAdd ){
			if ( newChildName.equals(name) ){
				
			}
		}
	}
}
*/
