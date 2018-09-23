package methods;

import java.util.ArrayList;

import kparser.devel.Utilities;
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
		String text = "Williams was reluctant to repeat what she had said to the official";
		GraphPassingNode gpn = sp.parse(text);
//		System.out.println(gpn.getSentence());
//		for(String s : gpn.getAspGraph()){
//			System.out.println(s);
//		}
		
		Utilities.saveObject(gpn, "gpn_example.ser");
		System.out.println("objectSaved");
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
