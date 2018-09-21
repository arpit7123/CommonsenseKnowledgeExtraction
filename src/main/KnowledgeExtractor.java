/**
 * 
 */
package main;

import helper.InputRep;
import helper.KnowledgeObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import methods.ASPBasedExtractor;


/**
 * @author Arpit Sharma
 * @date Aug 3, 2017
 *
 */
public class KnowledgeExtractor {
	
	private ASPBasedExtractor ke = null;

//	static{
//		instance = new KnowledgeExtractor();
//	}
//	
//	public static KnowledgeExtractor getInstance(){
//		return instance;
//	}
	
	public KnowledgeExtractor(){
		ke = new ASPBasedExtractor();
	}
	
	public ArrayList<KnowledgeObject> extractKnowledge(String inputText) throws Exception{
		return ke.getKnowledgeFromText(inputText);
	}
	
	public ArrayList<InputRep> extractKnowledge(InputRep sRep,InputRep qRep){
		return null;
	}
	
	public HashMap<String,ArrayList<KnowledgeObject>> extractKnowledge(File textFile) throws Exception{
		HashMap<String,ArrayList<KnowledgeObject>> result = new HashMap<String, ArrayList<KnowledgeObject>>();
		try(BufferedReader br = new BufferedReader(new FileReader(textFile))){
			String inputText = null;
			while((inputText=br.readLine())!=null){
				ArrayList<KnowledgeObject> knowList = ke.getKnowledgeFromText(inputText);
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
		ArrayList<KnowledgeObject> ks = kew.extractKnowledge(sent);
		if(ks!=null){
			for(KnowledgeObject kIs : ks){
				for(String k : kIs.getRoot().getHasTriples()){
					System.out.println(k);
				}
				System.out.println("******************************");
			}
		}
		System.exit(0);
		
	}

}
