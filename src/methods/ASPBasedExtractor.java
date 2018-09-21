package methods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import helper.DiscourseInfo;
import helper.KnowledgeObject;
import utils.ASP2KnowObject;
import utils.ASPDataPreparer;
import utils.ClingoExecutor;
import utils.Configurations;
import utils.EventSubgraphsExtractor;
import utils.LocalKparser;
import utils.TextPreprocessor;
import module.graph.helper.GraphPassingNode;
import module.graph.helper.JAWSutility;
import module.graph.helper.Node;

/**
 * @author Arpit Sharma
 *
 */
public class ASPBasedExtractor {

	private static String tempFileName = Configurations.getProperty("knowextfile");//"/home/arpit/workspace/WinogradPhd/Clingo/know_ext/tempFile.txt";
	private static String aspRulesFileName = Configurations.getProperty("knowextrulesfile");//"/home/arpit/workspace/WinogradPhd/Clingo/know_ext/rulesFile.txt";

//	private static final int words_chars_limit = 40;

	//	private SentenceToGraph stg = null;
	private LocalKparser lk = null;
//	private SentenceToGraph stg = null;
	private EventSubgraphsExtractor eventExtractor = null;
	private ASPDataPreparer aspPreparer = null;
	private ClingoExecutor clingoWrapper = null;
	private TextPreprocessor preprocessor = null;
	private ASP2KnowObject asp2KnowObj = null;

	public ASPBasedExtractor(){
		lk = new LocalKparser();
		eventExtractor = EventSubgraphsExtractor.getInstance();
		aspPreparer = ASPDataPreparer.getInstance();
		clingoWrapper = new ClingoExecutor(Configurations.getProperty("clingopath"));
		preprocessor = TextPreprocessor.getInstance();
		asp2KnowObj = ASP2KnowObject.getInstance();
	}

	public static void main_(String[] args){
		String word = "by";
		JAWSutility j = new JAWSutility();
		System.out.println(j.getBaseForm(word, "v"));

	}

//	@SuppressWarnings("unused")
	public static void main(String[] args)throws Exception{
		ASPBasedExtractor main = new ASPBasedExtractor();
		String sentence = "Usually when I eat it is because I am hungry.";
		sentence = "Williams was reluctant to repeat what she had said to the official";
		ArrayList<String> know = main.getKnowledge(sentence);
		if(know!=null){
			for(String s : know){
				System.out.println(s);
			}
		}else{
			System.out.println("Knowledge not found!");
		}

//		String inFile = "/home/arpit/workspace/WinogradPhd/WebContent/WEB-INF/lib/WSC_data/type1_know_sents.txt";
//		String inFile = "/home/arpit/workspace/WinogradPhd/dataForCompre/knowExtEvalData.txt";
//		String inFile = "/home/arpit/workspace/WinogradPhd/WebContent/WEB-INF/lib/WSC_data/auto_knowledge/type3";
//		main.getAndPrintKnowledge(inFile);
		
		
		System.exit(0);
	}

	public void getAndPrintKnowledge(String inFile) throws Exception{
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){
			String line = null;
			int index = 1;
			while((line=br.readLine())!=null){
				if(line.trim().equals("")){
					System.out.println(index++);
					System.out.println();
					continue;
				}
				
				if(line.equals("NO")){
					System.out.println("No Knowledge Sentence Present!");
					continue;
				}
				
				ArrayList<String> know = getKnowledge(line);
				if(know!=null){
					for(String s : know){
						System.out.println(s);
					}
				}else{
					System.out.println("Knowledge not found!");
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void getKnowFromFile(String inputFile, String outputFile) throws Exception{
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
				BufferedReader br = new BufferedReader(new FileReader(inputFile))){
			String line = null;
			while((line=br.readLine())!=null){
				if(line.trim().equalsIgnoreCase("")){
					continue;
				}
				ArrayList<String> lines = preprocessor.breakParagraph(line);
				for(String sent : lines){
					ArrayList<String> knowledge = getKnowledge(sent.trim());
					if(knowledge.size()>0){
//						System.out.println(knowledge.size());
						for(String know : knowledge){
							bw.append(line);
							bw.newLine();
							bw.append(know);
							bw.newLine();
							bw.append("*******************************************\n");
						}
					}
				}
			}
			br.close();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();			
		}
	}
	
	public ArrayList<KnowledgeObject> getKnowledgeFromText(String inputText) throws Exception{
		ArrayList<KnowledgeObject> result = null;
		ArrayList<String> kInstances = getKnowledge(inputText);
//		for(String inst : kInstances){
//			System.out.println(inst);
//		}
		
		int i = 0;
		if(kInstances!=null){
			for(String kInstance : kInstances){
				if(i==0){
					result = new ArrayList<KnowledgeObject>();
				}
				String[] kInstArr = kInstance.split(" ");
				////////////////////On Nov 22 2017 /////////////////////////////////////////////////////
				// Adding postprocessing step to extract the knowledge instances which were not extracted due to incorrect parsing.
				// Example: answerEventsType1(positive,comforted_3,comfort,recipient,me_4,because,positive,upset_9,upset,recipient,me_4) is used to extract,
				// answerType3(positive,upset,comforted_3,comfort,recipient)
				kInstArr = postProcessKnow(kInstArr);
				/////////////////////////////////////////////////////////////////////////////////////////

				for(String kIns : kInstArr){
					KnowledgeObject knowledge = getCondKnowledge(kIns);
					if(knowledge!=null){
						result.add(knowledge);
					}
				}
				i++;
			}
		}
		
		return result;
	}

	public ArrayList<String> getKnowledge(String inputText) throws Exception{
		ArrayList<String> result = null;
		inputText = lk.preprocessText(inputText);
		
		inputText = preprocessor.removeParentheses(inputText);
		
		GraphPassingNode gpn = lk.extractGraphWithoutPrep(inputText, false, true, false, 0);
//		GraphPassingNode gpn = stg.extractGraph(inputText, false, true);
//		for(String s : gpn.getAspGraph()){
//			System.out.println(s);
//		}
		HashMap<String,ArrayList<String>> sameGenderRefs = preprocessor.simpleCoreference(gpn);
		
		HashMap<String,String> posMap = gpn.getposMap();
		HashMap<Integer,String> wordsMap = new HashMap<Integer, String>();
		for(String s : posMap.keySet()){
			Pattern wordPat = Pattern.compile("(.*)(-)([0-9]{1,7})");
			Matcher m = wordPat.matcher(s);
			if(m.matches()){
				wordsMap.put(Integer.parseInt(m.group(3).trim()),m.group(1).trim());
			}
		}

		StringBuffer sb = new StringBuffer();
		for(int i=1;i<=wordsMap.size();++i){
			sb.append(wordsMap.get(i)+" ");
		}

		ArrayList<Node> subgraphs = eventExtractor.extractEventGraphs(gpn);
		ArrayList<DiscourseInfo> discInfoList = preprocessor.divideUsingDiscConn(sb.toString().trim());
		for(DiscourseInfo discInfo : discInfoList){
//			System.out.println(discInfo.toString());
			if((discInfo)!=null){
//				System.out.println(discInfo.getLeftArg());
//				System.out.println(discInfo.getRightArg());

				aspPreparer.prepareASPFile(discInfo, subgraphs, tempFileName, sameGenderRefs,gpn);

				ArrayList<String> listOfFiles = Lists.newArrayList();
				listOfFiles.add(tempFileName);
				listOfFiles.add(aspRulesFileName);
//				long startASPReasoningTime = System.currentTimeMillis();
				ArrayList<String> ansFromASP = clingoWrapper.callASPusingFilesList(listOfFiles);
//				long endASPReasoningTime = System.currentTimeMillis();
//				System.out.print("ASP Reasoning Time = " + (endASPReasoningTime-startASPReasoningTime));
				if(ansFromASP!=null){
					if(result==null){
						result = new ArrayList<String>();
					}
					result.addAll(ansFromASP);
//					if(ansFromASP.size()>0){
//						System.out.println(" YES");
//					}
				}else{
					System.err.println("Error in ASP module!!!");
				}
			}
		}
		return result;
	}
	
	
	public ArrayList<String> getKnowledge(String inputText, ArrayList<DiscourseInfo> discInfoList) throws Exception{
		ArrayList<String> result = new ArrayList<String>();
		inputText = preprocessor.removeParentheses(inputText);
		
		GraphPassingNode gpn = lk.extractGraphWithoutPrep(inputText, false, true, false, 0);
		
		HashMap<String,ArrayList<String>> mapOfLists = preprocessor.simpleCoreference(gpn);
		
		HashMap<String,String> posMap = gpn.getposMap();
		HashMap<Integer,String> wordsMap = new HashMap<Integer, String>();
		for(String s : posMap.keySet()){
			Pattern wordPat = Pattern.compile("(.*)(-)([0-9]{1,7})");
			Matcher m = wordPat.matcher(s);
			if(m.matches()){
				wordsMap.put(Integer.parseInt(m.group(3).trim()),m.group(1).trim());
			}
		}

		StringBuffer sb = new StringBuffer();
		for(int i=1;i<=wordsMap.size();++i){
			sb.append(wordsMap.get(i)+" ");
		}

		ArrayList<Node> subgraphs = eventExtractor.extractEventGraphs(gpn);
		for(DiscourseInfo discInfo : discInfoList){
			if((discInfo)!=null){
//				System.out.println(discInfo.getLeftArg());
//				System.out.println(discInfo.getRightArg());

				aspPreparer.prepareASPFile(discInfo, subgraphs, tempFileName, mapOfLists, gpn);

				ArrayList<String> listOfFiles = Lists.newArrayList();
				listOfFiles.add(tempFileName);
				listOfFiles.add(aspRulesFileName);
				result.addAll(clingoWrapper.callASPusingFilesList(listOfFiles));
			}
		}
		return result;
	}
	
	
	public String[] postProcessKnow(String[] kInstances){
		ArrayList<String> res = lk.postProcessKnow(kInstances);
		String[] result = new String[res.size()];
		for(int i=0;i<res.size();++i){
			result[i] = res.get(i);
		}
		return result;
	}
	
	public KnowledgeObject getCondKnowledge(String kInstance){
		KnowledgeObject knowledge = asp2KnowObj.processASPIntoGraph(kInstance, null);
//		if(knowledge!=null){
//			knowledge.getRDFKnowledge();
//		}
		return knowledge;
	}

	//	public static void main__(String[] args){
	//		PDTBResource pdtbRes = PDTBResource.getInstance();
	//		String inputText = "I spread the cloth on the table in order to protect it.";
	//		String pennTree = "(ROOT (S (NP (PRP I)) (VP (VBD spread) (NP (DT the) (NN cloth)) (PP (IN on) (NP (DT the) (NN table))) (SBAR (IN in) (NN order) (S (VP (TO to) (VP (VB protect) (NP (PRP it))))))) (. .)))";
	//		try{
	//			ArrayList<ConnectivesClass> listOfConns = pdtbRes.getConnAndArgs(inputText, pennTree);
	//			for(ConnectivesClass conn : listOfConns){
	//				System.out.println(conn.getConn());
	//			}
	//		}catch(Exception e){
	//			e.printStackTrace();
	//		}
	//	}

}

