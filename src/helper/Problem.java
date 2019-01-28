package helper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Problem {
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String sentence = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String question = null;
}
