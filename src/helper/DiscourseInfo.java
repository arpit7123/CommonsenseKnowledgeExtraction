/**
 * This class is used to save 
 */
package helper;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Arpit Sharma
 *
 */
public class DiscourseInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7993955310294100033L;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String leftArg = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String conn = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String rightArg = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String initConn = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String midConn = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String endConn = null;
	
	public DiscourseInfo(){}
	
	public DiscourseInfo(String leftArg, String conn, String rightArg){
		this.leftArg = leftArg;
		this.conn = conn;
		this.rightArg = rightArg;
	}
	
	@Override
	public String toString(){
		String result = "";
		if(initConn==null){
			if(midConn==null){
				if(endConn==null){
					result = "Left Arg: "+leftArg+"\nConn: "+conn+"\nRight Arg: "+rightArg;
				}
			}else{
				if(endConn!=null){
					result = "Left Arg: "+leftArg+"\nMid Conn: "+midConn+"\nRight Arg: "+rightArg+"\nEnd Conn: "+endConn;
				}
			}
		}else{
			if(midConn!=null){
				result = "\nInit Conn: "+initConn + "Left Arg: "+leftArg+"\nMid Conn: "+midConn+"\nRight Arg: "+rightArg;
			}
		}
		return result;
	}
}
