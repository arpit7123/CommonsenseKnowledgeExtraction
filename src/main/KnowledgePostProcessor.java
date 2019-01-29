package main;

import helper.ActionsInfo;
import helper.GraphNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import utils.Configurations;
import utils.GPN2Graph;

import com.mongodb.client.FindIterable;

import methods.KBOperations;
import methods.SentenceParser;
import module.graph.helper.GraphPassingNode;

public class KnowledgePostProcessor {

	private KBOperations kbo = null;
	private SentenceParser sentParser = null;
	private GPN2Graph gpn2Graph = null;
	private Pattern wordPat = Pattern.compile("(.*)(-)([0-9]{1,7})");

	public KnowledgePostProcessor(){
		kbo = new KBOperations();
		sentParser = SentenceParser.getInstance();
		gpn2Graph = GPN2Graph.getInstance();
	}

	@SuppressWarnings("unchecked")
	public void process(String[] parts, String filteredKnowDir){
		String dbQuery = "{type:'" + parts[0] + " " + parts[1] + " " + parts[2] +"'}";
		FindIterable<Document> fi = kbo.findInKB(dbQuery);

		//		boolean skip = true;
		int counter = 1;
		String filteredKnowFile = filteredKnowDir + counter;
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(filteredKnowFile));
			int knowCount = 0;
			for(Document doc : fi){
				System.out.println(knowCount++);
				if(knowCount<Integer.parseInt(Configurations.getProperty("skip_until"))){
					continue;
				}

				if(knowCount>Integer.parseInt(Configurations.getProperty("dbquery_limit"))){
					bw.close();
					return;
				}
				ArrayList<String> texts = (ArrayList<String>) doc.get("texts");
				String sentence = texts.get(0);
				//				System.out.println(sentence);
				//				System.out.println(action1+" : "+action1Rel+" : "+action2+" : "+action2Rel);
				//				String s = "The newspaper was founded with the name '''Angtong''' in the year Wednesday , as a weekly newspaper , but due to the opposition of the title by KYO , the name was changed to '''Thekar''' .";
				//				if(!sentence.equalsIgnoreCase(s)){
				//					if(skip){
				//						continue;
				//					}
				//				}else{
				//					skip=false;
				//				}
				try{
					GraphPassingNode gpn = sentParser.parse(sentence);
					ArrayList<GraphNode> graphs = gpn2Graph.getGraphs(gpn);

					Document know = (Document) doc.get("knowledge");
					String action1 = null;
					String action1Rel = null;
					String action2 = null;
					String action2Rel = null;
					String prop = null;

					Set<String> keys = know.keySet();
					for(String key : keys){
						if(key.equals("value")){
							if(parts[0].equals("action")){
								action1 = know.get("value").toString();
							}else if(parts[0].equals("prop")){
								prop = know.get("value").toString();
							}
						}else if(key.equals(parts[1].replaceAll(" ","_"))){
							Document part2Obj = (Document) know.get(key);
							Set<String> newKeys = part2Obj.keySet();
							for(String newKey : newKeys){
								if(newKey.equals("value")){
									if(parts[2].equals("action")){
										action2 = part2Obj.get(newKey).toString();
									}else if(parts[2].equals("prop")){
										prop = part2Obj.get(newKey).toString();
									}
								}else{
									if(parts[2].equals("action")){
										action2Rel = newKey;
									}
								}
							}
						}else if(parts[0].equals("action")){
							action1Rel = key;
						}
					}

					ActionsInfo ai = null;
					for(GraphNode root : graphs){
						if(ai==null){
							ai = findActionsInfo(root,action1,action1Rel,action2,action2Rel);
						}else{
							if(action1!=null && action2!=null && ai.getAction1Map()!=null && ai.getAction2Map()!=null){
								break;
							}

							if(action1!=null && prop!=null && ai.getAction1Map()!=null){
								break;
							}

							if(action2!=null && prop!=null && ai.getAction2Map()!=null){
								break;
							}

							ActionsInfo aiTemp = findActionsInfo(root,action1,action1Rel,action2,action2Rel);
							if(action1!=null && action2!=null){
								if(ai.getAction1Map()==null && aiTemp.getAction1Map()!=null){
									if(aiTemp.getAction1Map().size()>1){
										ai.setAction1Map(aiTemp.getAction1Map());
									}
								}

								if(ai.getAction2Map()==null && aiTemp.getAction2Map()!=null){
									if(aiTemp.getAction2Map().size()>1){
										ai.setAction2Map(aiTemp.getAction2Map());
									}
								}
							}

							if(action1!=null && action2==null){
								if(ai.getAction1Map()==null && aiTemp.getAction1Map()!=null){
									if(aiTemp.getAction1Map().size()>2){
										ai.setAction1Map(aiTemp.getAction1Map());
									}
								}
							}

							if(action1==null && action2!=null){
								if(ai.getAction2Map()==null && aiTemp.getAction2Map()!=null){
									if(aiTemp.getAction2Map().size()>2){
										ai.setAction2Map(aiTemp.getAction2Map());
									}
								}
							}
						}

					}

					String filteredKnow = filterKnowledge(gpn.getSentence(), ai, action1, action2, prop, parts[1]);
					bw.write(filteredKnow+"\t"+sentence);
					bw.newLine();
				}catch(Exception e){
					System.err.println("Error in filtering!");
				}

				counter++;
				if(counter%Integer.parseInt(Configurations.getProperty("know_file_splitter"))==0){
					bw.close();
					bw = new BufferedWriter(new FileWriter(filteredKnowDir+counter));
				}

			}

			bw.close();

		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private String preprocess(String sent){
		sent = sent.replaceAll("'s"," 's ");
		sent = sent.replaceAll(" +", " ");
		return sent;
	}

	private String processTreeMap(TreeMap<Integer,String> map, String[] words){
		String part = "";
		int indx = 0;
		boolean first = true;
		for(int key : map.keySet()){
			String val = map.get(key);
			if(first){
				if(val.matches("(.*)(-)([0-9]{1,3})")){
					part += words[key-1] + " ";
				}else{
					part += val + " ";
				}
				first = false;
				if(key!=1){
					indx = key-1;
				}
				continue;
			}
			int diff = key-(indx+1);
			if(diff>1 && diff<4){
				for(int i=indx+1;i<key-1;i++){
					part += words[i] + " ";
				}
			}
			if(val.matches("(.*)(-)([0-9]{1,3})")){
				part += words[key-1] + " ";
			}else{
				part += val + " ";
			}				
			indx = key-1;
		}
		return part;
	}

	private String filterKnowledge(String sent, ActionsInfo ai, String action1, String action2, String prop, String relation) throws Exception{
		System.out.println(sent);
		sent = preprocess(sent);

		String result = null;
		String[] sentWords = sent.split(" ");

		TreeMap<Integer,String> t1 = ai.getAction1Map();
		TreeMap<Integer,String> t2 = ai.getAction2Map();
		System.out.println(t1);
		System.out.println(t2);

		String part1 = null;
		String part2 = null;
		if(action1!=null && action2!=null){
			t1.remove(-1);
			t2.remove(-1);
			part1 = processTreeMap(t1, sentWords);
			part2 = processTreeMap(t2, sentWords);
		}else if(action1==null && action2!=null){
			String entType = t2.remove(-1);
			part1 = entType +" is "+prop +" ";
			part2 = processTreeMap(t2, sentWords);
		}else if(action1!=null && action2==null){
			String entType = t1.remove(-1);
			part2 = entType +" is "+prop + " ";
			part1 = processTreeMap(t1, sentWords);
		}

		if(part1!=null && part2!=null){
			result = part1 + relation + " " + part2;
		}

		return result;
	}

	private TreeMap<Integer,String> getInfo(GraphNode node, String action, String actionRel){
		TreeMap<Integer,String> actionMap = null;
		String conClass = node.getConClass();
		if(action.equals(conClass)){
			String nodeVal = node.getValue();
			Matcher m = wordPat.matcher(nodeVal);
			if(m.matches()){
				actionMap = new TreeMap<Integer, String>();
				int indx = Integer.parseInt(m.group(3));
				actionMap.put(indx, action);
				ArrayList<String> edges = node.getEdgeLabels();
				ArrayList<GraphNode> children = node.getChildren();
				int childIndx = 0;
				for(String edge : edges){
					if(edge.equals(actionRel)){
						GraphNode child = children.get(childIndx);
						String childValue = child.getValue();
						m = wordPat.matcher(childValue);
						if(m.matches()){
							int indx1 = Integer.parseInt(m.group(3));
							String childClass = child.getSuperClass();
							if(childClass!=null){
								actionMap.put(indx1, childClass);
								actionMap.put(-1, childClass);
							}else{
								actionMap.put(indx1, childValue);
								actionMap.put(-1, childValue);
							}
						}
					}else{
						GraphNode child = children.get(childIndx);
						String childValue = child.getValue();
						m = wordPat.matcher(childValue);
						if(m.matches()){
							int indx1 = Integer.parseInt(m.group(3));
							if(child.getPos().equalsIgnoreCase("nnp") || child.getPos().equalsIgnoreCase("nnps")){
								String childClass = child.getConClass();
								actionMap.put(indx1, childClass);
							}else{
								actionMap.put(indx1, childValue);
							}
						}
					}
					childIndx++;
				}
			}
		}
		return actionMap;
	}


	private ActionsInfo findActionsInfo(GraphNode node, String action1, String action1Rel, String action2, String action2Rel){
		ActionsInfo result = new ActionsInfo();
		TreeMap<Integer,String> action1Map = null;
		TreeMap<Integer,String> action2Map = null;

		if(action1!=null){
			action1Map = getInfo(node, action1, action1Rel);
		}

		if(action2!=null){
			action2Map = getInfo(node, action2, action2Rel);
		}

		if(action1Map!=null){
			result.setAction1Map(action1Map);
		}

		if(action2Map!=null){
			result.setAction2Map(action2Map);
		}

		if(action1!=null && action2!=null){
			if((action1Map==null || action1Map.size()<3) && (action2Map==null || action2Map.size()<3)){
				for(GraphNode child : node.getChildren()){
					ActionsInfo res = findActionsInfo(child,action1,action1Rel,action2,action2Rel);
					if(result.getAction1Map()==null){
						result.setAction1Map(res.getAction1Map());
					}
					if(result.getAction2Map()==null){
						result.setAction2Map(res.getAction2Map());
					}
				}
			}else if((action1Map==null || action1Map.size()<3) && (action2Map!=null && action2Map.size()>2)){
				for(GraphNode child : node.getChildren()){
					ActionsInfo tmpAi = findActionsInfo(child,action1,action1Rel,action2,action2Rel);
					TreeMap<Integer,String> tmpTree1 = tmpAi.getAction1Map();
					if(tmpTree1!=null && tmpTree1.size()>2){
						result.setAction1Map(tmpTree1);
					}
				}
			}else if((action1Map!=null && action1Map.size()>2) && (action2Map==null || action2Map.size()<3)){
				for(GraphNode child : node.getChildren()){
					ActionsInfo tmpAi = findActionsInfo(child,action1,action1Rel,action2,action2Rel);
					TreeMap<Integer,String> tmpTree2 = tmpAi.getAction2Map();
					if(tmpTree2!=null && tmpTree2.size()>2){
						result.setAction2Map(tmpTree2);
					}
				}
			}
		}else if(action1==null && action2!=null){// && prop!=null){
			if(action2Map==null || action2Map.size()<3){
				for(GraphNode child : node.getChildren()){
					ActionsInfo tmpAi = findActionsInfo(child,action1,action1Rel,action2,action2Rel);
					TreeMap<Integer,String> tmpTree2 = tmpAi.getAction2Map();
					if(tmpTree2!=null && tmpTree2.size()>2){
						result.setAction2Map(tmpTree2);
					}
				}
			}
		}else if(action1!=null && action2==null){// && prop!=null){
			if(action1Map==null || action1Map.size()<3){
				for(GraphNode child : node.getChildren()){
					ActionsInfo tmpAi = findActionsInfo(child,action1,action1Rel,action2,action2Rel);
					TreeMap<Integer,String> tmpTree1 = tmpAi.getAction1Map();
					if(tmpTree1!=null && tmpTree1.size()>2){
						result.setAction1Map(tmpTree1);
					}
				}
			}			
		}

		return result;
	}

	public static void main(String args[]) throws Exception{
		KnowledgePostProcessor kpp = new KnowledgePostProcessor();
		String filteredKnowFile = Configurations.getProperty("filteredKnow");
		String queryPart1 = Configurations.getProperty("dbquery_part1");
		String queryPart2 = Configurations.getProperty("dbquery_part2");
		String queryPart3 = Configurations.getProperty("dbquery_part3");
		String[] queryParts = {queryPart1,queryPart2,queryPart3};
		kpp.process(queryParts,filteredKnowFile);


		//		String sentence = "Specifically , the NTSB faulted the captain for failing to take control of the aircraft or abort the landing earlier , noting that the captain had warnings at 500 feet and at 100 to 200 feet and could have aborted the landing at that time .";
		//		sentence = "Incensed at Tinatin's decision , he further disowned her children and favored his offspring of his second marriage , giving rise to a family feud after his death in Wednesday .";
		//		GraphPassingNode gpn = kpp.sentParser.parse(sentence);
		//		sentence = gpn.getSentence();
		//		GraphPassingNode gpn = new GraphPassingNode(null,null,sentence,null); 

		//		ActionsInfo ai = new ActionsInfo();
		//		
		//		TreeMap<Integer,String> t1 = new TreeMap<Integer,String>();
		//		t1.put(1,"Specifically-1");{-1=person, 7=person, 13=favor, 15=offspring-15}
		//		t1.put(4,"NTSB");
		//		t1.put(5,"fault");
		//		t1.put(7,"person");
		//		t1.put(9,"failing-9");
		//		
		//		TreeMap<Integer,String> t2 = new TreeMap<Integer,String>();
		//		t2.put(25,"person");
		//		t2.put(38,"could-38");
		//		t2.put(39,"have-39");
		//		t2.put(40,"abort");
		//		t2.put(42,"landing-42");
		//		t2.put(45,"time-45");
		//		
		//		TreeMap<Integer,String> t1 = new TreeMap<Integer,String>();
		//		t1.put(-1,"person");
		//		t1.put(7,"person");
		//		t1.put(13,"favour");
		//		t1.put(15,"offspring-15");
		//		
		//		TreeMap<Integer,String> t2 = null;
		//		ai.setAction1Map(t1);
		//		ai.setAction2Map(t2);
		//		System.out.println(kpp.filterKnowledge(sentence, ai, "favour", null, "further", "and"));




		System.exit(0);
	}

}
