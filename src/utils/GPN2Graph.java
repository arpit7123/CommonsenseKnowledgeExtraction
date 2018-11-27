package utils;

import helper.GraphNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import module.graph.helper.ClassesResource;
import module.graph.helper.GraphPassingNode;

public class GPN2Graph {

	private HashMap<String, HashSet<String>> childrenMap;
	private HashMap<String,Boolean> hasParentMap;
	private HashSet<String> wordsSet;
	private HashMap<String,HashMap<String,String>> edgeMap;
	private HashMap<String,String> posMap;
	private HashMap<String,String> classMap;
	private HashMap<String,String> superClassMap;
//	private Pattern wordPat = Pattern.compile("(.*)(-)([0-9]{1,7})");
	
	/**
	 * a regex pattern to read the RDF style <i>has(X,R,Y)</i> statements.
	 */
	private static Pattern p = Pattern.compile("(has\\()(.*)(\\).)");
	private static GPN2Graph gpn2Graph = null;
	
	static {
		gpn2Graph = new GPN2Graph();
	}
	
	public static GPN2Graph getInstance(){
		return gpn2Graph;
	}
	
	private GPN2Graph() {
		childrenMap = new HashMap<String, HashSet<String>>();
		hasParentMap = new HashMap<String,Boolean>();
		wordsSet = new HashSet<String>();
		edgeMap = new HashMap<String,HashMap<String,String>>();
	}
	
	public ArrayList<GraphNode> getGraphs(GraphPassingNode gpn){
		ArrayList<GraphNode> parseTreeList = new ArrayList<GraphNode>();
		if(gpn!=null){
			posMap = gpn.getposMap();
			ClassesResource cr = gpn.getConClassRes();
			classMap = cr.getClassesMap();
			superClassMap = cr.getSuperclassesMap();
			ArrayList<String> aspGraph = gpn.getAspGraph();
//			String sentence = gpn.getSentence();
			populateGlobalMaps(aspGraph);
			parseTreeList = generateParseTreeList();
		}else{
			System.out.println("NULL");
		}
		return parseTreeList;
	}
	
	/**
	 * This is a method to generate the list of objective graphs from the RDF style graph output from SentenceToGraph class.
	 * @param inputArray it is an ArrayList of RDF style graph
	 * @return parseTreeList it is an ArrayList of Root nodes of output graphs.
	 */
	private void populateGlobalMaps(ArrayList<String> inputArray){
		childrenMap.clear();
		hasParentMap.clear();
		wordsSet.clear();
		edgeMap.clear();
		
		for (String line : inputArray) {
			Matcher m = p.matcher(line);
			if(m.find()){
				String[] s = m.group(2).split(",");
				String parent = s[0];
				String edge = s[1];
				String child = s[2];
				if(!edge.equalsIgnoreCase("semantic_role") 
						&& !edge.equalsIgnoreCase("instance_of") 
						&& !edge.equalsIgnoreCase("is_subclass_of")){	
					if(parent.equalsIgnoreCase(child)){
						child = "_"+child;
						if(posMap.containsKey(child)){
							posMap.put(child, posMap.get(child));
						}else{
							posMap.put(child, "UNK");
						}
					}
					
					wordsSet.add(parent);
					wordsSet.add(child);
					
					HashMap<String,String> temp = null;
					if(edgeMap.containsKey(parent)){
						temp = edgeMap.get(parent);
					}else{
						temp = new HashMap<String,String>();
					}
					temp.put(child, edge);
					edgeMap.put(parent,temp);
					
					HashSet<String> childrenList = null;
					if(childrenMap.containsKey(parent)){
						childrenList = childrenMap.get(parent);
					}else{
						childrenList = new HashSet<String>();
					}
					childrenList.add(child);
					childrenMap.put(parent, childrenList);
					hasParentMap.put(child, true);
					
				}
			}
		}
	}
	
	/**
	 * This is a method to generate the list of objective graphs from the RDF style graph output from SentenceToGraph class.
	 * @param inputArray it is an ArrayList of RDF style graph
	 * @return parseTreeList it is an ArrayList of Root nodes of output graphs.
	 */
	private ArrayList<GraphNode> generateParseTreeList() {
		ArrayList<GraphNode> parseTreeList = new ArrayList<GraphNode>();

		ArrayList<String> roots = new ArrayList<String>();

		for(String s : wordsSet){
			if(!hasParentMap.containsKey(s)){
				roots.add(s);
			}
		}

		for(String s : roots){
			GraphNode node = createGraphNode(s);
			node.setPos(posMap.get(s));
			createParseTree(node,s);//, rootToken, semanticsMap, true);
			parseTreeList.add(node);
		}

		return parseTreeList;
	}

	/**
	 * This method recursively creates the parse graph using the childrenMap and root node.
	 * @param node it is a node of the graph.
	 * @param nodeValue it is the value of the node
	 */	
	private void createParseTree(GraphNode node, String nodeValue){
		if(childrenMap.containsKey(nodeValue)){
			HashSet<String> children = childrenMap.get(nodeValue);
			for(String child : children){
				GraphNode n = new GraphNode(child);
				
				if(superClassMap!=null){
					if(superClassMap.containsKey(child)){
						n.setSuperClass(superClassMap.get(child));
					}
				}
				
				if(classMap!=null){
					if(classMap.containsKey(child)){
						n.setConClass(classMap.get(child));
					}
				}
				
				n.setPos(posMap.get(child));
				node.addChild(n);
				node.addEdgeLabel(edgeMap.get(nodeValue).get(child));
				if(childrenMap.containsKey(child)){
					createParseTree(n,child);
				}
			}
		}
		
	}
	
	/**
	 *  This method Creates a parse graph node 
	 * @param contentItems it is the label of the graph node.
	 * @return GraphNode it is the graph node.
	 */
	private GraphNode createGraphNode(String nodeValue) {
		GraphNode node = new GraphNode(nodeValue);
		if(superClassMap!=null){
			if(superClassMap.containsKey(nodeValue)){
				node.setSuperClass(superClassMap.get(nodeValue));
			}
		}
		
		if(classMap!=null){
			if(classMap.containsKey(nodeValue)){
				node.setConClass(classMap.get(nodeValue));
			}
		}
		return node;
	}

}
