package helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

//kparser.jar
import module.graph.helper.Node;

public class InputRep implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9165397043531121522L;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String type = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private HashSet<String> rdfTriples = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private Node graphRoot = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private ArrayList<ArrayList<String>> knowledgeIns = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private HashMap<String,ArrayList<String>> extraStuff = null;
	
	@Override
	public String toString(){
		String result = null;
		if(rdfTriples!=null && rdfTriples.size()>0){
			result = "";
			for(String s : rdfTriples){
				result += s+"\n";
			}
		}
		return result;
	}
}
