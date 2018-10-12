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
import utils.TextPreprocessor;
import module.graph.helper.GraphPassingNode;
import module.graph.helper.Node;

/**
 * @author Arpit Sharma
 *
 */
public class ASPBasedExtractor {

	private static String tempFileName = Configurations.getProperty("knowextfile");//"/home/arpit/workspace/WinogradPhd/Clingo/know_ext/tempFile.txt";
	private static String aspRulesFileName = Configurations.getProperty("knowextrulesfile");//"/home/arpit/workspace/WinogradPhd/Clingo/know_ext/rulesFile.txt";

//	private static final int words_chars_limit = 40;

	private EventSubgraphsExtractor eventExtractor = null;
	private ASPDataPreparer aspPreparer = null;
	private ClingoExecutor clingoWrapper = null;
	private TextPreprocessor preprocessor = null;
	private ASP2KnowObject asp2KnowObj = null;
	private SentenceParser sentParser = null;

	public ASPBasedExtractor(){
		eventExtractor = EventSubgraphsExtractor.getInstance();
		aspPreparer = ASPDataPreparer.getInstance();
		clingoWrapper = new ClingoExecutor(Configurations.getProperty("clingopath"));
		preprocessor = TextPreprocessor.getInstance();
		asp2KnowObj = ASP2KnowObject.getInstance();
		sentParser = SentenceParser.getInstance();
	}

	public static void main(String[] args)throws Exception{
		ASPBasedExtractor main = new ASPBasedExtractor();
		String sentence = "Usually when I eat it is because I am hungry.";
		sentence = "Williams was reluctant to repeat what she had said to the official";
		sentence = "Tom was bullying James, for this reason alone Tom was punished";
//		sentence = "Tom was bullying James and consequently we punished Tom.";
		ArrayList<String> know = main.getKnowledge(sentence,null,null);
		if(know!=null){
			for(String s : know){
				System.out.println(s);
			}
		}else{
			System.out.println("Knowledge not found!");
		}
		
		System.exit(0);
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
					ArrayList<String> knowledge = getKnowledge(sent.trim(),null,null);
					if(knowledge.size()>0){
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
	
	public ArrayList<KnowledgeObject> getKnowledgeFromText(String inputText, GraphPassingNode gpn, ArrayList<DiscourseInfo> discInfoList) throws Exception{
		ArrayList<KnowledgeObject> result = null;
		ArrayList<String> kInstances = getKnowledge(inputText,gpn,discInfoList);
		
//		for(String kInst : kInstances){
//			System.out.println(kInst);
//		}
		
		int i = 0;
		if(kInstances!=null){
			for(String kInstance : kInstances){
				if(i==0){
					result = new ArrayList<KnowledgeObject>();
				}
				String[] kInstArr = kInstance.split("\\) type");
				////////////////////On Nov 22 2017 /////////////////////////////////////////////////////
				// Adding postprocessing step to extract the knowledge instances which were not extracted due to incorrect parsing.
				// Example: answerEventsType1(positive,comforted_3,comfort,recipient,me_4,because,positive,upset_9,upset,recipient,me_4) is used to extract,
				// answerType3(positive,upset,comforted_3,comfort,recipient)
				kInstArr = postProcessKnow(kInstArr);
				/////////////////////////////////////////////////////////////////////////////////////////

				for(String kIns : kInstArr){
					KnowledgeObject knowledge = getCondKnowledge(kIns);
					if(knowledge!=null){
						knowledge.setText(inputText);
						result.add(knowledge);
					}
				}
				i++;
			}
		}
		
		return result;
	}

	public ArrayList<String> getKnowledge(String inputText, GraphPassingNode gpn, ArrayList<DiscourseInfo> discInfoList) throws Exception{
		ArrayList<String> result = null;
		if(gpn==null){
			gpn = sentParser.parse(inputText);
		}
		inputText = gpn.getSentence();
		
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
		if(discInfoList==null){
			discInfoList = preprocessor.divideUsingDiscConn(sb.toString().trim());
		}
		for(DiscourseInfo discInfo : discInfoList){
			if((discInfo)!=null){
//				System.out.println(discInfo.getConn());
				aspPreparer.prepareASPFile(discInfo,subgraphs,tempFileName,sameGenderRefs,gpn);
				ArrayList<String> listOfFiles = Lists.newArrayList();
				listOfFiles.add(tempFileName);
				listOfFiles.add(aspRulesFileName);
				ArrayList<String> ansFromASP = clingoWrapper.callASPusingFilesList(listOfFiles);
				if(ansFromASP!=null){
					if(result==null){
						result = new ArrayList<String>();
					}
					result.addAll(ansFromASP);
				}else{
					System.err.println("Error in ASP module!!!");
				}
			}
		}
		return result;
	}
	
	public String[] postProcessKnow(String[] kInstances){
		ArrayList<String> res = sentParser.postProcessKnow(kInstances);
		String[] result = new String[res.size()];
		for(int i=0;i<res.size();++i){
			result[i] = res.get(i);
		}
		return result;
	}
	
	public KnowledgeObject getCondKnowledge(String kInstance){
		KnowledgeObject knowledge = asp2KnowObj.processASPIntoGraph(kInstance, null);
		return knowledge;
	}

}

