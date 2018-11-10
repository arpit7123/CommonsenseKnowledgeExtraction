/**
 * 
 */
package main;

import helper.DiscourseInfo;
import helper.KnowledgeObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utils.Configurations;
import kparser.devel.Utilities;
import methods.ASPBasedExtractor;
import methods.KBOperations;
import module.graph.helper.GraphPassingNode;


/**
 * @author Arpit Sharma
 * @date Aug 3, 2017
 *
 */
public class KnowledgeExtractor {
	
	private String savedKparsesdirPath = null;
	
	private ASPBasedExtractor ke = null;
	private KBOperations kbo = null;
	
	public KnowledgeExtractor(){
		savedKparsesdirPath = Configurations.getProperty("kparseroutdatadir");
		ke = new ASPBasedExtractor();
		kbo = new KBOperations();
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
	
	@SuppressWarnings("unchecked")
	public void runOnServer() throws IOException{
		String fileNamesFilePath = Configurations.getProperty("knowextfrom");
		int counter = 1;
		ArrayList<String> kparseFile = Utilities.listFilesForFolder(new File(savedKparsesdirPath), false);
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(fileNamesFilePath, true));
		HashSet<String> setOfDoneFiles = getSetOfDoneFiles(fileNamesFilePath);
		java.util.Collections.sort(kparseFile);
		for(String listFile : kparseFile){
			if(!setOfDoneFiles.contains(listFile)){
				System.out.println("Processing file "+listFile);
				bw.write(listFile);
				bw.newLine();
				bw.flush();
	
				ArrayList<GraphPassingNode> listOfParses = (ArrayList<GraphPassingNode>) Utilities.load(savedKparsesdirPath+listFile);
				for(GraphPassingNode gpn : listOfParses){
					ArrayList<KnowledgeObject> kObjList;
					try {
						kObjList = extractKnowledge(gpn.getSentence(),gpn,null);
						if(kObjList!=null){
							for(KnowledgeObject kObj : kObjList){
								kbo.updateKB(kObj);
//								System.out.println(counter);
								counter++;
							}
						}
					} catch (Exception e) {
						//					e.printStackTrace();
						System.err.println("Error in knowledge extraction!");
						//					e.printStackTrace();
					}
				}
			} else {
				System.out.println("skipping file " + listFile);
			}
		}

		bw.close();
	}
	
	public HashSet<String> getSetOfDoneFiles(String fileNamesFilePath) throws IOException{
		HashSet<String> result = new HashSet<String>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(fileNamesFilePath));
			String line = null;
			while((line=br.readLine())!=null){
				result.add(line);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		finally {
			br.close();
		}
		return result;
	}
	
	public void extractAndSaveToDB(String text){
		ArrayList<KnowledgeObject> kObjList;
		try {
			kObjList = extractKnowledge(text,null,null);
			if(kObjList!=null){
				for(KnowledgeObject kObj : kObjList){
					kbo.updateKB(kObj);
				}
			}
		} catch (Exception e) {
			System.err.println("Error in knowledge extraction!");
		}
		System.out.println("Knowledge successfully extracted and saved to DB");
	}
	
	public void extractAndPrint(String text) throws Exception{
		ArrayList<KnowledgeObject> ks = extractKnowledge(text,null,null);
		if(ks!=null){
			for(KnowledgeObject kIs : ks){
				System.out.println(kIs.getType());
				for(String k : kIs.getRoot().getHasTriples()){
					System.out.println(k);
				}
				System.out.println("******************************");
				System.out.println(kIs.getRoot().getJSONObject().toJSONString());
			}
		}
	}
	
	public void extFrmKparserObjAndPrint(String kparserObjFile) throws Exception{
//		kparserObjFile = "gpn_example.ser";
		GraphPassingNode gpn = (GraphPassingNode)Utilities.load(kparserObjFile);
		ArrayList<KnowledgeObject> ks = extractKnowledge(gpn.getSentence(),gpn,null);
		if(ks!=null){
			for(KnowledgeObject kIs : ks){
				System.out.println(kIs.getType());
				for(String k : kIs.getRoot().getHasTriples()){
					System.out.println(k);
				}
				System.out.println("******************************");
				System.out.println(kIs.getRoot().getJSONObject().toJSONString());
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		KnowledgeExtractor kew = new KnowledgeExtractor();
		String sent = "The driver then may have decided not to lift it because it was too heavy.";
		sent = "For example, a maintenance van with heavy equipment in the back may not balance well on an asymmetric unit.";
		sent = "Williams was reluctant to repeat what she had said to the official";
		sent = "Hannah was bullying Claire, for this reason alone Hannah was punished";
		
		kew.runOnServer();
//		kew.extractAndSaveToDB(sent);
		
		
		System.exit(0);
		
	}

}
