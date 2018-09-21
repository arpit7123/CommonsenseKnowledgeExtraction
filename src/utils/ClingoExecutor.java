/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
		for(String name : listOfFiles){
			files = files + " " + name;
		}
		command = clingoPath + " " +files.trim();
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
	
	public ArrayList<String> callASPusingCmd(String cmmd) throws InterruptedException{// sentFile, String bkFile, String rulesFile){
//		System.out.println(cmmd);
		command = cmmd;//clingoPath + " " +cmmd;
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
		
//		String sentRepASPFile = "/home/arpit/workspace/WinogradPhd/Clingo/sent.txt";
//		String quesRepASPFile = "/home/arpit/workspace/WinogradPhd/Clingo/ques.txt";
//		String knowRepASPFile = "/home/arpit/workspace/WinogradPhd/Clingo/know.txt";
//		String clingoPath = "/home/arpit/workspace/WinogradPhd/Clingo/clingo";
//		String reasoningRulesPath = "/home/arpit/workspace/WinogradPhd/Clingo/just_rules.txt";
		String clingo = "/home/arpit/workspace/WinogradPhd/WebContent/WEB-INF/lib/Clingo/clingo";
		
		ClingoExecutor cw = new ClingoExecutor(clingo);
//		
//		ArrayList<String> list = new ArrayList<String>(4);
//		for(int i=0;i<4;++i){
//			list.add("");
//		}
//		list.set(0, reasoningRulesPath);
//		list.set(1, sentRepASPFile);
//		list.set(2, quesRepASPFile);
//		list.set(3, knowRepASPFile);
//		
//		ArrayList<String> res = null;
//		try {
//			res = cw.callASPusingFilesList(list);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		if(res!=null){
//			System.out.println("Knowledge found:");
//			for(String s : res){
//				System.out.println(s);
//			}
//		}
//		System.out.println("Finished !!!");		
//
//		System.exit(0);
		String rules = "/home/arpit/workspace/WinogradPhd/WebContent/WEB-INF/lib/Clingo/reasoning/just_rules.txt";
		
		ArrayList<String> listOfFiles = new ArrayList<String>();
		listOfFiles.add(rules);
		listOfFiles.add("1.txt");
		
		ArrayList<String> res = cw.callASPusingFilesList(listOfFiles);
		for(String s : res){
			System.out.println(s);
		}
		
//		for(int i=1;i<=5;++i){
//			String command = clingo + " " + rules + " " + i + ".txt";
//			String[] cmd = {
//					"/bin/sh",
//					"-c",
//					command
//			};
//			Process process;
//			process = Runtime.getRuntime().exec(cmd);
//			process.waitFor();
//
//			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String line = null;
//			while((line=stdInput.readLine())!=null){
//				System.out.println(line);
//			}
//		}
		

	}

}
