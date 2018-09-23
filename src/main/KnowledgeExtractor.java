/**
 * 
 */
package main;

import helper.DiscourseInfo;
import helper.KnowledgeObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import kparser.devel.Utilities;
import methods.ASPBasedExtractor;
import module.graph.helper.GraphPassingNode;


/**
 * @author Arpit Sharma
 * @date Aug 3, 2017
 *
 */
public class KnowledgeExtractor {
	
	private ASPBasedExtractor ke = null;

	public KnowledgeExtractor(){
		ke = new ASPBasedExtractor();
	}
	
	public ArrayList<KnowledgeObject> extractKnowledge(String inputText, GraphPassingNode parse, ArrayList<DiscourseInfo> discInfoList) throws Exception{
		return ke.getKnowledgeFromText(inputText,parse,discInfoList);
	}
	
	public HashMap<String,ArrayList<KnowledgeObject>> extractKnowledge(File textFile) throws Exception{
		HashMap<String,ArrayList<KnowledgeObject>> result = new HashMap<String, ArrayList<KnowledgeObject>>();
		try(BufferedReader br = new BufferedReader(new FileReader(textFile))){
			String inputText = null;
			while((inputText=br.readLine())!=null){
				ArrayList<KnowledgeObject> knowList = ke.getKnowledgeFromText(inputText,null,null);
				result.put(inputText, knowList);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		KnowledgeExtractor kew = new KnowledgeExtractor();
		String sent = "The driver then may have decided not to lift it because it was too heavy.";
		sent = "For example, a maintenance van with heavy equipment in the back may not balance well on an asymmetric unit.";
		sent = "Williams was reluctant to repeat what she had said to the official";
		GraphPassingNode gpn = (GraphPassingNode)Utilities.load("gpn_example.ser");
		ArrayList<KnowledgeObject> ks = kew.extractKnowledge(sent,gpn,null);
		if(ks!=null){
			for(KnowledgeObject kIs : ks){
				System.out.println(kIs.getType());
				for(String k : kIs.getRoot().getHasTriples()){
					System.out.println(k);
				}
				System.out.println("******************************");
			}
		}
		System.exit(0);
		
	}

}
