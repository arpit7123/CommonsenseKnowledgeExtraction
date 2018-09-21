package helper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class KnowledgeObject {
	
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) public String type = null;
	@Getter (AccessLevel.PUBLIC) @Setter (AccessLevel.PUBLIC) public KnowledgeGraphNode root = null;
}
