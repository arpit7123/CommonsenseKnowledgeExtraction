/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Arpit Sharma
 * @date Aug 4, 2017
 *
 */
public class ClingoExecutor {

	enum Enum_OS {
		LINUX,
		WINDOWS,
		UNKNOWN;
	}

	private String command = "";
	private String clingoPath = null;
	private Enum_OS currentOS = getOS();

	public ClingoExecutor(){}
	
	public ClingoExecutor(String clingoPath){
		this.clingoPath = clingoPath;
	}

	public Enum_OS getOS() {
		String OS = System.getProperty("os.name").toLowerCase(); 
		Enum_OS currentOS;
		if (OS.contains("linux")) { 
			currentOS = Enum_OS.LINUX;
		} else if (OS.contains("windows")) { 
			currentOS = Enum_OS.WINDOWS;
		} else {
			currentOS = Enum_OS.UNKNOWN;
			System.err.println("Unknown Operating System"); 
		}
		return currentOS;
	}

	public ArrayList<String> callASPusingFilesList(ArrayList<String> listOfFiles) throws InterruptedException{// sentFile, String bkFile, String rulesFile){
		String files = "";
		String[] cmd;
		for(String name : listOfFiles){
			files = files + " " + name;
		}
		command = clingoPath + " " +files.trim();
		ArrayList<String> result = null;
		try {
			switch (currentOS) {
			case LINUX:
				cmd = new String[]{
						"/bin/sh",
						"-c",
						command
				};
				break;
			case WINDOWS:
				cmd = new String[]{
						"cmd",
						"/c",
						command
				};
				break;
			default:
				throw new IOException("Unknown Operating System");
			}

			Process process;
			process = Runtime.getRuntime().exec(cmd);
			if(!process.waitFor(5, TimeUnit.SECONDS)) {
				System.err.println("Clingo timed out");
				return null;
			}

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while((line=stdInput.readLine())!=null){
				Pattern p = Pattern.compile(".*Answer.*");
				Matcher m = p.matcher(line);
				if(m.find()){
					if(result==null){
						result=new ArrayList<String>();
					}
					line=stdInput.readLine();
					if(!line.trim().equalsIgnoreCase("")){
						result.add(line);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public ArrayList<String> callASPusingCmd(String cmmd) throws InterruptedException{// sentFile, String bkFile, String rulesFile){
		command = cmmd;
		ArrayList<String> result = null;
		try {
			switch (currentOS) {
			case LINUX:
				String[] cmd = {
						"/bin/sh",
						"-c",
						command
				};
				Process process;
				process = Runtime.getRuntime().exec(cmd);
				process.waitFor();

				BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while((line=stdInput.readLine())!=null){
					Pattern p = Pattern.compile(".*Answer.*");
					Matcher m = p.matcher(line);
					if(m.find()){
						if(result==null){
							result=new ArrayList<String>();
						}
						line=stdInput.readLine();
						if(!line.trim().equalsIgnoreCase("")){
							result.add(line);
						}
					}
				}
				break;
			default:
				@SuppressWarnings("unused")
				String[] cmd2 = {
						"cmd",
						"/c",
						command
				};
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	public static void main(String[] args) throws IOException, InterruptedException{
		String clingo = "/home/arpit/workspace/WinogradPhd/WebContent/WEB-INF/lib/Clingo/clingo";
		ClingoExecutor cw = new ClingoExecutor(clingo);
		String rules = "/home/arpit/workspace/WinogradPhd/WebContent/WEB-INF/lib/Clingo/reasoning/just_rules.txt";
		
		ArrayList<String> listOfFiles = new ArrayList<String>();
		listOfFiles.add(rules);
		listOfFiles.add("1.txt");
		
		ArrayList<String> res = cw.callASPusingFilesList(listOfFiles);
		for(String s : res){
			System.out.println(s);
		}
	}

}
