package helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.simple.JSONObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class KnowledgeGraphNode implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3919127785487890770L;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) private String value;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) private String lemma;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) private String superclass;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) private String postag;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) private ArrayList<String> edges;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) private ArrayList<KnowledgeGraphNode> children;
	
	public String getTraversal(){
		String result = "";
		
		
		return result;
	}
	
	public HashSet<String> getHasTriples(){
		HashSet<String> result = new HashSet<String>();
		result.add("has(" + value + ",instance_of," + superclass + ").");
		if(edges!=null){
			for(int i=0;i<edges.size();++i){
				String edge = edges.get(i);
				KnowledgeGraphNode child = children.get(i);
				result.add("has(" + value + "," + edge + "," + child.value + ").");
				result.addAll(child.getHasTriples());
			}
		}
		return result;
	}
	
	public JSONObject getJSONObject(){
		JSONObject obj = traverseANode(this);
		return obj;
	}
	
	public String getJSONString(){
		return traverseANode(this).toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject traverseANode(KnowledgeGraphNode node){
		JSONObject obj = new JSONObject();
		obj.put("value", node.getSuperclass());
		ArrayList<String> edges = node.getEdges();
		ArrayList<KnowledgeGraphNode> children = node.getChildren();
		if(edges!=null){
			for(int i=0;i<edges.size();i++){
				KnowledgeGraphNode child = children.get(i);
				obj.put(edges.get(i),traverseANode(child));
			}
		}
		return obj;
	}
	
	
	@Override
	public String toString(){
		return getTraversal();
	}
}
