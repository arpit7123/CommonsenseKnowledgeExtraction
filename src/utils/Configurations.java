/**
 * 
 */
package utils;

/**
 * @author Arpit Sharma
 * @date Aug 7, 2017
 *
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Configurations {
	
	private static String configFile = null;
	private static Properties props = null;
	static{
		configFile = "config.properties";
		props = new Properties();
		InputStream input = null;
		try {
			// load a properties file
			input = new FileInputStream(configFile);
			props.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getProperty(String propName){
		return props.getProperty(propName);
	}
	
	
  public static void main(String[] args) {
	  System.out.println(Configurations.getProperty("clingopath"));

  }
}