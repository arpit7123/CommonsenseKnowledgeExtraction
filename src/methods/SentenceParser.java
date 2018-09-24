package methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import de.jollyday.config.Configuration;
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
	
	public static void main(String[] args) {
		SentenceParser sp = new SentenceParser();
		String dirPath = Configurations.getProperty("kparserdatadir");
		String outdirPath = Configurations.getProperty("kparseroutdatadir");
		File dir = new File(dirPath);
		ArrayList<String> fileNames = Utilities.listFilesForFolder(dir, false);
		
		
		for(String fileName : fileNames){
			ArrayList<GraphPassingNode> listOfParses = new ArrayList<GraphPassingNode>();
			try(BufferedReader br = new BufferedReader(new FileReader(dirPath+fileName))){
				String line = null;
				int lineNum = 1;
				while((line=br.readLine())!=null){
					if(line.trim().equals("")){
						continue;
					}
					try{
						GraphPassingNode parse = sp.parse(line);
						listOfParses.add(parse);
						if(lineNum%2000==0){
							Utilities.saveObject(listOfParses, outdirPath+lineNum);
							listOfParses.clear();
						}
						lineNum++;
						System.out.println(lineNum);
					}catch(Exception e){
						System.out.println("Error Encountered!");
						if(listOfParses.size()>0){
							Utilities.saveObject(listOfParses, outdirPath+lineNum);
							listOfParses.clear();
						}
						lineNum++;
					}					
				}
				if(listOfParses.size()>0){
					System.out.println("Final");
					Utilities.saveObject(listOfParses, outdirPath+lineNum);
				}
			}catch(Exception e){
				System.out.println("Error Encountered!");
			}
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

}
