package methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

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
		SentenceParser sp = new SentenceParser();
		String dirPath = Configurations.getProperty("kparserdatadir");
		String outdirPath = Configurations.getProperty("kparseroutdatadir");
		File dir = new File(dirPath);
		ArrayList<String> fileNames = Utilities.listFilesForFolder(dir, false);
		
		int sentIndx = 1;
		for(String fileName : fileNames){
			ArrayList<GraphPassingNode> listOfParses = new ArrayList<GraphPassingNode>();
			try(BufferedReader br = new BufferedReader(new FileReader(dirPath+fileName))){
				String line = null;
				while((line=br.readLine())!=null){
					if(line.trim().equals("")){
						continue;
					}
					try{
						GraphPassingNode parse = sp.parse(line);
						listOfParses.add(parse);
						if(sentIndx%2000==0){
							Utilities.saveObject(listOfParses, outdirPath+sentIndx);
							listOfParses.clear();
						}
						sentIndx++;
						System.out.println(sentIndx);
					}catch(Exception e){
						System.out.println("Error Encountered!");
						if(listOfParses.size()>0){
							Utilities.saveObject(listOfParses, outdirPath+sentIndx);
							listOfParses.clear();
						}
						sentIndx++;
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

}
