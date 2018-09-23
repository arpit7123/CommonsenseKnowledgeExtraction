package helper;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class KnowledgeObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9073594681312569267L;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) public String type = null;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) public KnowledgeGraphNode root = null;
}
