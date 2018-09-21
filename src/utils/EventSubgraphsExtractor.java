/**
 * 
 */
package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import module.graph.helper.ClassesResource;
import module.graph.helper.GraphPassingNode;
import module.graph.helper.Node;

/**
 * @author Arpit Sharma
 *
 */
public class EventSubgraphsExtractor {

	private HashMap<String, HashSet<String>> childrenMap;
	private HashMap<String,Boolean> hasParentMap;
	private HashSet<String> wordsSet;
	private HashMap<String,HashMap<String,String>> edgeMap;
	public HashMap<String,String> posMap;
	public HashMap<String,ArrayList<String>> wordSenseMap;
	public HashMap<String,String> classMap;
	public HashMap<String,String> superClassMap;
	private Pattern wordPat = Pattern.compile("(.*)(-)([0-9]{1,7})");
	private HashSet<String> globalWordSet;
	
	/**
	 * a regex pattern to read the RDF style <i>has(X,R,Y)</i> statements.
	 */
	private static Pattern p = Pattern.compile("(has\\()(.*)(\\).)");
	private static EventSubgraphsExtractor eventExtractor = null;
	
	static {
		eventExtractor = new EventSubgraphsExtractor();
	}
	
	public static EventSubgraphsExtractor getInstance(){
		return eventExtractor;
	}
	
	private EventSubgraphsExtractor() {
		childrenMap = new HashMap<String, HashSet<String>>();
		hasParentMap = new HashMap<String,Boolean>();
		wordsSet = new HashSet<String>();
		edgeMap = new HashMap<String,HashMap<String,String>>();
		globalWordSet = new HashSet<String>();
	}
	
	public ArrayList<Node> extractEventGraphs(GraphPassingNode gpn) throws Exception{
		ArrayList<Node> parseTreeList = new ArrayList<Node>();
		if(gpn!=null){
			posMap = gpn.getposMap();
			wordSenseMap = gpn.getWordSenseMap();
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
	public void populateGlobalMaps(ArrayList<String> inputArray) throws Exception {
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
				if(!edge.equalsIgnoreCase("semantic_role")){	
					if(parent.equalsIgnoreCase(child)){
						child = "_"+child;
						if( this.posMap.containsKey(s[2])){
							this.posMap.put(child, this.posMap.get(s[2]));
						}
					}
					
					wordsSet.add(parent);
					wordsSet.add(child);
					
					if(edgeMap.containsKey(parent)){
						HashMap<String,String> temp = edgeMap.get(parent);
						temp.put(child, edge);
						edgeMap.put(parent,temp);
					}else{
						HashMap<String,String> temp = new HashMap<String,String>();
						temp.put(child, edge);
						edgeMap.put(parent,temp);
					}
					
					if(childrenMap.containsKey(parent)){
						HashSet<String> childrenList = childrenMap.get(parent);
						childrenList.add(child);
						childrenMap.put(parent, childrenList);
						hasParentMap.put(child, true);
					}else{
						HashSet<String> childrenList = new HashSet<String>();
						childrenList.add(child);
						childrenMap.put(parent, childrenList);
						hasParentMap.put(child, true);
					}
					
				}
			}
		}
	}
	
	/**
	 * This is a method to generate the list of objective graphs from the RDF style graph output from SentenceToGraph class.
	 * @param inputArray it is an ArrayList of RDF style graph
	 * @return parseTreeList it is an ArrayList of Root nodes of output graphs.
	 */
	public ArrayList<Node> generateParseTreeList() {
		ArrayList<Node> parseTreeList = new ArrayList<Node>();

		ArrayList<String> roots = new ArrayList<String>();

		for(String s : wordsSet){
//			if(!hasParentMap.containsKey(s)){
//				roots.add(s);
//			}
			if(posMap.containsKey(s)){
				String tmpPos = posMap.get(s);
//				if(tmpPos!=null){
					if(tmpPos.startsWith("V") && s.matches("(.*)(-)([0-9]{1,7})")){
						roots.add(s);
					}
//				}else{
//					System.out.println(s);
//				}
			}
		}

		for(String s : roots){
			globalWordSet.clear();
			Node node = createGraphNode(s);
			Matcher m = wordPat.matcher(s);
			if(m.matches()){
				String wsdKey = m.group(1)+"_"+m.group(3);
				if(this.wordSenseMap.containsKey(wsdKey)){
					node.setWordSense(this.wordSenseMap.get(wsdKey).get(0));
				}
			}
			
			node.setPOS(posMap.get(s));//removed on 9th June 2015 :- .replaceAll("_", "-")));

//			if(this.wordSenseMap.containsKey(s)){
//				String tempSense = this.wordSenseMap.get(s).get(0);
//				node.setWordSense(tempSense);
//			}
			
//			removed on 9th June 2015 :-
//			if(posMap.get(s.replaceAll("_", "-")).startsWith("V")){ 
			if(posMap.get(s).startsWith("V")){
				node.setEvent(true);
			}else{
				node.setEntity(true);
			}
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
	private void createParseTree(Node node, String nodeValue){
		if(childrenMap.containsKey(nodeValue) && !globalWordSet.contains(nodeValue)){
			if(nodeValue.matches("(.*)(-)([0-9]{1,7})")){
				globalWordSet.add(nodeValue);
			}
			HashSet<String> children = childrenMap.get(nodeValue);
			for(String child : children){
				Node n = new Node(child);
				
				if(this.superClassMap!=null){
					if(this.superClassMap.containsKey(child)){
						n.setSuperClass(this.superClassMap.get(child));
					}
				}
//				removed on 9th June 2015 :-
//				n.setPOS(posMap.get(child.replaceAll("_", "-")));
				n.setPOS(posMap.get(child));
				
//				System.out.println(child);
				Matcher m = wordPat.matcher(child);
				if(m.matches()){
					String wsdKey = (m.group(1)+"_"+m.group(3)).toLowerCase();
					if(this.wordSenseMap.containsKey(wsdKey)){
						n.setWordSense(this.wordSenseMap.get(wsdKey).get(0));
					}
				}
				
				boolean isAValidChild = true;
				if(edgeMap.get(nodeValue).get(child).equalsIgnoreCase("instance_of")
						||edgeMap.get(nodeValue).get(child).equalsIgnoreCase("prototype_of")){
					n.setClass(true);
					node.setLemma(child);
//					isAValidChild=false;
				}else if (edgeMap.get(nodeValue).get(child).equalsIgnoreCase("is_subclass_of")){
					n.setClass(true);
//					isAValidChild=false;
				}else if(edgeMap.get(nodeValue).get(child).equalsIgnoreCase("semantic_role")){
					n.setASemanticRole(true);
					isAValidChild=false;
				} else if(edgeMap.get(nodeValue).get(child).equalsIgnoreCase("has_coreferent")) {
					n.setACoreferent(true);
					isAValidChild=false;
				}else {
//					removed on 9th June 2015 :-
//					if(posMap.get(child.replaceAll("_", "-")).startsWith("V")){
					if(posMap.get(child).startsWith("V")){
						n.setEvent(true);
						isAValidChild=false;
					}else{
						n.setEntity(true);
					}
				}

				if(isAValidChild){
					node.addChild(n);
					node.addEdgeName(edgeMap.get(nodeValue).get(child));
					if(childrenMap.containsKey(child)){
						createParseTree(n,child);
					}
				}
			}
		}
		
	}
	
	/**
	 *  This method Creates a parse graph node 
	 * @param contentItems it is the label of the graph node.
	 * @return GraphNode it is the graph node.
	 */
	private Node createGraphNode(String contentItems) {
		Node node = new Node(contentItems);
		if(this.superClassMap!=null){
			if(this.superClassMap.containsKey(contentItems)){
				node.setSuperClass(this.superClassMap.get(contentItems));
			}
		}
		return node;
	}
}
