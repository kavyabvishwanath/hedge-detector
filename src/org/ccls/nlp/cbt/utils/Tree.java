package org.ccls.nlp.cbt.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.token.type.Token;
import org.uimafit.util.JCasUtil;

public class Tree implements Cloneable {
	private Node root;
	private List<Node> allNodes;
	
	public Tree() {
		root = null;
		allNodes = null;
	}

	public Tree(DependencyNode node) {
		this.root = new Node();
		this.root.data = node;
		this.root.setParent(null);
		this.root.setChildren(new ArrayList<Node>());
		this.root.setLevel(0);
		
		this.allNodes = new ArrayList<Node>();
	}
	
	public Tree(Node node) {
		this.root = node;

		this.allNodes = new ArrayList<Node>();
	}

/******************************** The tree formation methods******************************
*
*  These take a node anywhere in a tree and form the appropriate depth of tree around it
*/	
	// please do not do BOTH formTree and formFullTree
	// this is surprisingly fast, but we do have the results stored to disc
	public void formFullTree() {
		// this helps us set the node type
		int initialLevel = 0;
		// address is used in equals method for comparison
		int initialAddress = root.getData().getAddress();
		// parent path
		List<Integer> addressPathToRoot = new ArrayList<Integer>();
		do {
			FSArray heads = root.getData().getHeadRelations();
			if (heads == null || heads.size() == 0) {
			   break;
			}
			
			DependencyNode head = ((DependencyRelation) heads.get(0)).getHead();

			if (head.equals(root.getData())) {
				break;
			}

			addressPathToRoot.add(head.getAddress());
			root.setData(head);
			initialLevel++;
		} while (true);
		
		if (initialLevel > 1) {
			root.setType(Node.NodeType.HYPERPARENT);
		} else if (initialLevel == 1) {
			root.setType(Node.NodeType.PARENT);
		} else {
			root.setType(Node.NodeType.SELF);
		}
		
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		allNodes.add(root);
		while (queue.size() > 0) {
			Node current = queue.remove(0);
			int level = current.getLevel();
			DependencyNode currentData = current.getData();
			int address = currentData.getAddress();
			if (level < initialLevel) {
				if (addressPathToRoot.contains(address)) {
					if (level + 1 == initialLevel) {
						current.setType(Node.NodeType.PARENT);
					} else {
						current.setType(Node.NodeType.HYPERPARENT);						
					}
				} else {
					current.setType(Node.NodeType.OOL);
				}
			}
			FSArray childs = currentData.getChildRelations();
			int childSize = childs.size();
			for (int index = 0; index < childSize; index++) {
				DependencyNode child = ((DependencyRelation) childs.get(index)).getChild();
				if (child.equals(currentData)) {
					continue;
				}
				Node childNode = new Node();
				childNode.setData(child);
				childNode.setChildren(new ArrayList<Node>());
				childNode.setParent(current);				
				childNode.setLevel(level + 1);
				if (current.getType() == Node.NodeType.SELF) {
					childNode.setType(Node.NodeType.CHILD);
				} else if (current.getType() == Node.NodeType.PARENT) {
					if (child.getAddress() == initialAddress) {
						childNode.setType(Node.NodeType.SELF);
					} else {
						childNode.setType(Node.NodeType.SIBLING);						
					}					
				} else if (current.getType() == Node.NodeType.OOL && level + 1 == initialLevel) {
					childNode.setType(Node.NodeType.COUSIN);									
				} else if (current.getType() == Node.NodeType.CHILD || current.getType() == Node.NodeType.HYPOCHILD) {
					childNode.setType(Node.NodeType.HYPOCHILD);					
				} else if (current.getType() == Node.NodeType.COUSIN || current.getType() == Node.NodeType.SIBLING || current.getType() == Node.NodeType.YOL) {
					childNode.setType(Node.NodeType.YOL);					
				}
				
				current.getChildren().add(childNode);
				
				queue.add(childNode);
				allNodes.add(childNode);
			}
		}
	}

//********************************TREE SUBSET METHODS**************************************
	
	// return the children tree
	public Tree getChildrenTree() {		
		
		// 1 find the self node
		Tree tree = null;
		try {
			tree = (Tree) this.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		for (Node node : tree.allNodes) {
			if (node.getType() == Node.NodeType.SELF) {
				try {
					tree.root = (Node) node.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					System.exit(1);
				}				
				break;
			}
		}
				
		// 2 prune tree
		tree.root.parent = null;
		for (Node child : tree.root.children) {
			child.getChildren().clear();
		}
		
		return tree;
	}
	
	// return the parent tree
	public Tree getParentTree() {
		// 1 find the self node
		Tree tree = null;
		try {
			tree = (Tree) this.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		for (Node node : tree.allNodes) {
			if (node.getType() == Node.NodeType.SELF) {
				try {
					tree.root = (Node) node.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					System.exit(1);
				}				
				break;
			}
		}
		
		// 2 prune tree
		
		tree.root.children.clear();
		if (tree.root.parent != null) {
			int selfAddress = tree.root.getData().getAddress();
			tree.root.parent.parent = null;
			Iterator<Node> childrenIterator = tree.root.parent.getChildren().iterator();
			while (childrenIterator.hasNext()) {
				Node child = childrenIterator.next();
				int address = child.getData().getAddress();
				if (address != selfAddress) {
					childrenIterator.remove();
				}
			}
		}
		
		return tree;		
	}

	// return the siblings tree
	// I am split on 2 ways of doing this
	// Way 1:
	//                 P
	//	             /  \
	//              T    S
	//
	// Way 2:
	//
	//              T----S
	//
	// I think I will implement way 1 for now, but maybe both are warranted.
	
	public Tree getSiblingTree() {
		// 1 find the self node
		Tree tree = null;
		try {
			tree = (Tree) this.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		for (Node node : tree.allNodes) {
			if (node.getType() == Node.NodeType.SELF) {
				try {
					tree.root = (Node) node.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				}				
				break;
			}
		}
		
		// 2 prune tree
		
		tree.root.children.clear();
		if (tree.root.parent != null) {
			tree.root.parent.parent = null;
			for (int c = 0; c < tree.root.parent.children.size(); c++) {
				tree.root.parent.children.get(c).setChildren(new ArrayList<Node>());
			}
		}
		
		tree.root = tree.root == null ? null : tree.root.parent;
		
		return tree;
	}

	public Tree getDepAncestorTree() {
		// 1 find the self node
		Tree tree = null;
		try {
			tree = (Tree) this.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		for (Node node : tree.allNodes) {
			if (node.getType() == Node.NodeType.SELF) {
				try {
					tree.root = (Node) node.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				}
				break;
			}
		}

		// 2 prune tree
		tree.root.children.clear();
		while (tree.root.parent != null) {
			Node curr = tree.root;
			tree.root.parent.children.clear();
			tree.root.parent.children.add(curr);
			tree.root = tree.root.parent;
		}
		return tree;
	}

/*****************************TREE DISPLAY METHODS*******************************/	

	public Map<String, String> getFeatureRepresentations(JCas jCas, String namePrefix) {
		Map<String, String> toReturn = new HashMap<String, String>();
		List<Node> queue = new ArrayList<Node>();

		if (root == null) {
			return toReturn;
		}
		queue.add(root);
		StringBuilder sb1 = new StringBuilder("(" + namePrefix + "_" + "LEMMA");
		StringBuilder sb2 = new StringBuilder("(" + namePrefix + "_" + "POS");
		int edges = 0;
		while (queue.size() > 0) {
			Node current = queue.get(0);
			Token token = JCasUtil.selectCovered(jCas, Token.class, current.getData()).get(0);
			if (current.getChildren() != null) {
				for (Node child : queue.get(0).getChildren()) {
					queue.add(child);
				}
			}
			if (current.getParent() != null) {
				Token parentToken = JCasUtil.selectCovered(jCas, Token.class, current.getParent().getData()).get(0);
				sb1.append(" (" + parentToken.getLemma() + " " + token.getLemma() + ")");
				sb2.append(" (" + parentToken.getPos() + " " + token.getPos() + ")");
				edges++;
			}
			queue.remove(0);
		}
		sb1.append(")");
		sb2.append(")");
		
		if (edges > 0) {
			//System.out.println(sb1.toString());
			//System.out.println(sb2.toString());
			
			toReturn.put(namePrefix + "_Lemma", StringUtils.normalizeSpace(sb1.toString()));
			toReturn.put(namePrefix + "_Pos", StringUtils.normalizeSpace(sb2.toString()));
		}
		
		return toReturn;
	}	
	
	public void printTree(JCas jCas, PrintStream ps) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		StringBuilder sb = new StringBuilder();
		sb.append("__________");
		while (queue.size() > 0) {
			Node current = queue.get(0);
			if (current.getChildren() != null) {
				for (Node child : queue.get(0).getChildren()) {
					queue.add(child);
				}
			}
			Token token = JCasUtil.selectCovered(jCas, Token.class, current.getData()).get(0);
			sb.append(System.getProperty("line.separator") + token.getLemma() + "," + token.getPos() + " --> " + current.getLevel() + "," + current.getType());	
			queue.remove(0);
		}
		sb.append(System.getProperty("line.separator") + "**********" + System.getProperty("line.separator"));
		ps.println(sb.toString());
	}
	
	public static void printEmptyTree(PrintStream ps) {
		StringBuilder sb = new StringBuilder();
		sb.append("__________");
		sb.append(System.getProperty("line.separator") + "**********" + System.getProperty("line.separator"));
		ps.println(sb.toString());
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Tree t = new Tree();
		t.root = (Node) root.clone();
		t.allNodes = new ArrayList<Node>();
		for (Node node : allNodes) {
			t.allNodes.add(node);
		}
		return t;
	}
	
	public static class Node implements Cloneable {				

		public static enum NodeType { 
			HYPERPARENT, PARENT, SELF, SIBLING, CHILD, HYPOCHILD, COUSIN, OOL, YOL;
		}
		
		private DependencyNode data;
		private Node parent;
		private List<Node> children;
		private Integer level;
		private NodeType type;

		public DependencyNode getData() {
			return data;
		}
		public void setData(DependencyNode data) {
			this.data = data;
		}
		public Node getParent() {
			return parent;
		}
		public void setParent(Node parent) {
			this.parent = parent;
		}
		public List<Node> getChildren() {
			return children;
		}
		public void setChildren(List<Node> children) {
			this.children = children;
		}
		public Integer getLevel() {
			return level;
		}
		public void setLevel(Integer level) {
			this.level = level;
		}
		public NodeType getType() {
			return type;
		}
		public void setType(NodeType type) {
			this.type = type;
		}
		@Override
		protected Object clone() throws CloneNotSupportedException {
			Node node = new Node();
			node.setType(type);
			node.setLevel(level);
			node.setParent(parent == null ? null : (Node) parent.clone());
			node.setData(data);
			node.setChildren(new ArrayList<Node>());
			for (Node child : children) {
				node.getChildren().add(child);
			}
			return node;
		}
		@Override
		public String toString() {
			return "Node [data=" + data + ", parent=" + parent + ", children="
					+ children.size() + ", level=" + level + ", type=" + type + "]";
		}
	}
}
