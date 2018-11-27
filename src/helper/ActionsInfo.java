package helper;

import java.util.TreeMap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ActionsInfo {
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private TreeMap<Integer,String> action1Map = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private TreeMap<Integer,String> action2Map = null;
}
