package helper;

import java.util.ArrayList;
import java.util.HashSet;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class KnowledgeGraphNode {

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
	
	
	
	@Override
	public String toString(){
		return getTraversal();
	}
}
