package methods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import kparser.devel.Utilities;
import utils.Configurations;
import utils.LocalKparser;
import utils.TextPreprocessor;
import module.graph.helper.GraphPassingNode;

public class SentenceParser {
	
	private LocalKparser lk = null;
	private TextPreprocessor preprocessor = null;
	
	private static SentenceParser sentParser = null;
	
	static{
		sentParser = new SentenceParser();
	}

	public static SentenceParser getInstance(){
		return sentParser;
	}
	
	private SentenceParser(){
		lk = new LocalKparser();
		preprocessor = TextPreprocessor.getInstance();
	}
	
	public void runOnServer(){
		String dirPath = Configurations.getProperty("kparserdatadir");
		String outdirPath = Configurations.getProperty("kparseroutdatadir");
		String doneSentsFilePath = Configurations.getProperty("doneSentsFile");
		HashSet<String> doneSentsSet = populateDoneSents(doneSentsFilePath);
		
		File dir = new File(dirPath);
		ArrayList<String> fileNames = Utilities.listFilesForFolder(dir, false);
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(doneSentsFilePath))){
			int sentIndx = 1;
			for(String fileName : fileNames){
				ArrayList<GraphPassingNode> listOfParses = new ArrayList<GraphPassingNode>();
				try(BufferedReader br = new BufferedReader(new FileReader(dirPath+fileName))){
					String line = null;
					while((line=br.readLine())!=null){
						System.out.println(sentIndx);
						sentIndx++;
						if(doneSentsSet.contains(line)){
							continue;
						}
						
						bw.write(line);
						bw.newLine();
						
						if(line.trim().equals("")){
							continue;
						}
						try{
							GraphPassingNode parse = parse(line);
							listOfParses.add(parse);
							if(sentIndx%2000==0){
								Utilities.saveObject(listOfParses, outdirPath+sentIndx);
								listOfParses.clear();
							}
						}catch(Exception e){
							System.out.println("Error Encountered!");
							if(listOfParses.size()>0){
								Utilities.saveObject(listOfParses, outdirPath+sentIndx);
								listOfParses.clear();
							}
						}					
					}
					if(listOfParses.size()>0){
						System.out.println("Final");
						Utilities.saveObject(listOfParses, outdirPath+sentIndx);
					}
				}catch(Exception e){
					System.out.println("Error Encountered!");
				}
			}
			
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SentenceParser sp = new SentenceParser();
		String sentence = "John lifted Tom because Tom was not heavy.";
		
		GraphPassingNode gpn = sp.parse(sentence);
		for(String s : gpn.getAspGraph()){
			System.out.println(s);
		}
		
		
		
	}
	
	public GraphPassingNode parse(String inputText){
		inputText = lk.preprocessText(inputText);
		inputText = preprocessor.removeParentheses(inputText);
		GraphPassingNode result = lk.extractGraphWithoutPrep(inputText, false, true, false, 0);
		return result;
	}
	
	public ArrayList<String> postProcessKnow(String[] kInstances){
		ArrayList<String> result = lk.postProcessKnow(kInstances);
		return result;
	}
	
	public HashSet<String> populateDoneSents(String doneSentsFilePath){
		HashSet<String> result = new HashSet<String>();
		try(BufferedReader br = new BufferedReader(new FileReader(doneSentsFilePath))){
			String line = null;
			while((line=br.readLine())!=null){
				result.add(line);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return result;
	}

}
