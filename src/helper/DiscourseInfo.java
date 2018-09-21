/**
 * 
 */
package helper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Arpit Sharma
 *
 */
public class DiscourseInfo {
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String initConn = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String leftArg = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String conn = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String rightArg = null;
	
	public DiscourseInfo(){}
	
	public DiscourseInfo(String leftArg, String conn, String rightArg){
		this.leftArg = leftArg;
		this.conn = conn;
		this.rightArg = rightArg;
	}
	
	@Override
	public String toString(){
		String result = "Init Conn: "+initConn+"\nLeft Arg: "+leftArg+"\nConn: "+conn+"\nRight Arg: "+rightArg;
		return result;
	}
}
