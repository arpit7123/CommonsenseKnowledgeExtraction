package helper;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class GraphNode implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7222190031006438330L;
	
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String value = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String pos = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String conClass = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String superClass = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private ArrayList<GraphNode> children = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private ArrayList<String> edgeLabels = null;
	
	public GraphNode(String value){
		this.setValue(value);
		this.setChildren(new ArrayList<GraphNode>());
		this.setEdgeLabels(new ArrayList<String>());
	}
	
	public GraphNode(){
		this.setChildren(new ArrayList<GraphNode>());
		this.setEdgeLabels(new ArrayList<String>());
	}
	
	public void addChild(GraphNode node){
		this.children.add(node);
	}
	
	public void addEdgeLabel(String edgeLabel){
		this.edgeLabels.add(edgeLabel);
	}
	
}
