package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import module.graph.helper.Node;
import helper.KnowledgeGraphNode;
import helper.KnowledgeObject;
import utils.Configurations;

public class ASP2KnowObject {

	private HashMap<String,String> mapOfConns = null;
	private static ASP2KnowObject instance = null;

	public static ASP2KnowObject getInstance(){
		return instance;
	}

	static{
		instance = new ASP2KnowObject();
	}

	private ASP2KnowObject(){
		String connsFilePath = Configurations.getProperty("connspolarity");
		mapOfConns = populateMapOfConns(connsFilePath);
	}

	public static void main(String[] args) throws Exception {
		ASP2KnowObject obj = new ASP2KnowObject();

//		String sent = "I often feel as if I only get half the story sometimes so I typically do a massive amount of reporting that is most often much more than I will ever need";
//		sent = "he could not vote because he had been declared mentally incapacitated";
		String aspKnowledgeStr = "type1(positive,get_8,get,agent,i_6,so,positive,do_16,do,agent,i_14) type1(positive,feel_3,feel,agent,i_1,so,positive,do_16,do,agent,i_14)";
		aspKnowledgeStr = "type1(negative,vote_4,vote,agent,he_1,because,positive,declared_9,declare,recipient,he_6)";
		aspKnowledgeStr = "type2(positive,asymmetric_19,balance_15,balance,location) type2(positive,maintenance_5,balance_15,balance,agent) type3(positive,asymmetric_19,balance_15,balance,location) type3(positive,maintenance_5,balance_15,balance,agent) type9_1(positive,balance_15,balance,location,positive,asymmetric_19) type9_1(positive,balance_15,balance,agent,positive,maintenance_5)";
		String[] aspKnow = aspKnowledgeStr.split(" ");
		
		for(String know : aspKnow){
			KnowledgeObject ko = obj.processASPIntoGraph(know, null);
			HashSet<String> hasTriples = ko.getRoot().getHasTriples();
			System.out.println("Type: " + ko.getType());
			for(String triple : hasTriples){
				System.out.println(triple);
			}
		}
		System.exit(0);
	}


	@SuppressWarnings("unused")
	private void updatePrunedActionsMap(Node n, HashMap<String,Node> mapOfPrunedRoots){
		if(n.getPOS()!=null){
			if(n.getPOS().startsWith("V")){
				ArrayList<Node> children = n.getChildren();
				ArrayList<String> edges = n.getEdgeList();

				ArrayList<Node> newChildren = new  ArrayList<Node>();
				ArrayList<String> newEdges = new  ArrayList<String>();

				int i=0;
				for(Node child : children){
					Node nn = getNewChild(child);
					if(nn!=null){
						newChildren.add(nn);
						newEdges.add(edges.get(i));
					}
					i++;
				}
				n.setChildren(newChildren);
				n.setEdgeList(newEdges);

				mapOfPrunedRoots.put(n.getValue(), n);
			}

			ArrayList<Node> children = n.getChildren();
			for(Node child : children){
				updatePrunedActionsMap(child, mapOfPrunedRoots);
			}
		}
	}

	private Node getNewChild(Node n){
		ArrayList<Node> children = n.getChildren();
		ArrayList<String> edges = n.getEdgeList();

		ArrayList<Node> newChildren = new ArrayList<Node>();
		ArrayList<String> newEdges = new ArrayList<String>();

		int i = 0;
		for(Node child : children){
			Node nn = getNewChild(child);
			if(nn!=null){
				newChildren.add(nn);
				newEdges.add(edges.get(i));
			}
			i++;
		}
		if(n.getPOS()!=null){
			if(!n.getPOS().equals("NNP") 
					&& !n.getPOS().equals("NNPS")
					&& !n.getPOS().equals("PRP")
					&& !n.getPOS().equals("PRP$")
					&& !n.isAClass()
					&& !n.isASemanticRole()){
				n.setChildren(newChildren);
				n.setEdgeList(newEdges);
				return n;
			}
		}
		return null;
	}
	
	public KnowledgeObject processASPIntoGraph(String aspFormatKnow, HashMap<String,Node> mapOfPrunedRoots){
		KnowledgeObject knowledge = null;
		if(!aspFormatKnow.startsWith("type")){
//			aspFormatKnow = "type" + aspFormatKnow;
			return knowledge;
		}
		String knowPrefix = aspFormatKnow.substring(0, aspFormatKnow.indexOf("("));
		String knowCore = aspFormatKnow.substring(aspFormatKnow.indexOf("(")+1,aspFormatKnow.length()-1);
		if(knowCore.startsWith("\"")){
			knowCore = knowCore.substring(1);
		}
		if(knowCore.endsWith("\"")){
			knowCore = knowCore.substring(0, knowCore.length()-1);
		}
		
		String[] tmp = knowCore.split("\",\"");
		
//		for(int i=0;i<tmp.length;i++){
//			if(tmp[i].startsWith("\"") && tmp[i].endsWith("\"")){
//				tmp[i] = tmp[i].substring(1, tmp[i].length()-1);
//			}
//		}
		
		switch (knowPrefix){

		case "type1": // a property prevents an action
			if(tmp.length==5){
				String type = "prop prevents action";
				String trait = tmp[1];
				String traitLemma = trait;
				if(trait.matches("(.*)(-)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("-"));
				}else if(trait.matches("(.*)(_)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("_"));
				}
				
				boolean traitPolarity = false;
				if(tmp[0].equals("positive")){
					traitPolarity = true;
				}

				String event = tmp[2];
				String eventLemma = tmp[3];
				String eventRel = tmp[4];

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(trait);
				root.setLemma(traitLemma);
				root.setSuperclass(traitLemma);
				root.setPostag("j");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				if(!traitPolarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}

				edges.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children.add(ch2);
				
				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("prevents");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(event);
				ch3.setLemma(eventLemma);
				ch3.setSuperclass(eventLemma);
				ch3.setPostag("vb");
				children.add(ch3);
				
				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();

				edges2.add(eventRel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("nn");
				children2.add(ch22);

				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 1: " + aspFormatKnow);
			}

			break;
			
		case "type2": // an action causes another action
			if(tmp.length==11){
				String evnt1 = null;
				String evnt1Lemma = null;
				String evnt1Rel = null;
				boolean evnt1Polarity = false;

				String evnt2 = null;
				String evnt2Lemma = null;
				String evnt2Rel = null;
				boolean evnt2Polarity = false;

				String type = "action causes action";

				if(tmp[0].equals("positive") && tmp[6].equals("positive")){
					String conn_polarity = getConnPolarity(tmp[5]);
					if(conn_polarity != null){
						if(conn_polarity.equals("reverse")){
							evnt1 = tmp[7];
							evnt1Polarity = true;
							evnt1Rel = tmp[9];
							evnt1Lemma = tmp[8];

							evnt2 = tmp[1];
							evnt2Polarity = true;
							evnt2Rel = tmp[3];
							evnt2Lemma = tmp[2];
						}else{
							evnt1 = tmp[1];
							evnt1Polarity = true;
							evnt1Rel = tmp[3];
							evnt1Lemma = tmp[2];

							evnt2 = tmp[7];
							evnt2Polarity = true;
							evnt2Rel = tmp[9];
							evnt2Lemma = tmp[8];
						}
					}
				}else if(tmp[0].equals("positive") && !tmp[6].equals("positive")){
					if(getConnPolarity(tmp[5]).equals("reverse")){
						evnt1 = tmp[7];
						evnt1Lemma = tmp[8];
						evnt1Rel = tmp[9];

						evnt2Polarity = true;
						evnt2 = tmp[1];
						evnt2Lemma = tmp[2];
						evnt2Rel = tmp[3];

					}else{
						type = "action prevents action";
						evnt1Polarity = true;
						evnt1 = tmp[1];
						evnt1Lemma = tmp[2];
						evnt1Rel = tmp[3];

						evnt2Polarity = true;
						evnt2 = tmp[7];
						evnt2Lemma = tmp[8];
						evnt2Rel = tmp[9];
					}
				}else if(!tmp[0].equals("positive") && tmp[6].equals("positive")){
					if(getConnPolarity(tmp[5]).equals("reverse")){
						type = "action prevents action";
						evnt1Polarity = true;
						evnt1 = tmp[7];
						evnt1Lemma = tmp[8];
						evnt1Rel = tmp[9];

						evnt2Polarity = true;
						evnt2 = tmp[1];
						evnt2Lemma = tmp[2];
						evnt2Rel = tmp[3];
					}else{
						evnt1 = tmp[1];
						evnt1Lemma = tmp[2];
						evnt1Rel = tmp[3];

						evnt2Polarity = true;
						evnt2 = tmp[7];
						evnt2Lemma = tmp[8];
						evnt2Rel = tmp[9];
					}		
				}else {
					type = "action prevents action";
					if(getConnPolarity(tmp[5]).equals("reverse")){
						evnt1 = tmp[7];
						evnt1Lemma = tmp[8];
						evnt1Rel = tmp[9];

						evnt2Polarity = true;
						evnt2 = tmp[1];
						evnt2Lemma = tmp[2];
						evnt2Rel = tmp[3];
					}else{
						evnt1 = tmp[1];
						evnt1Lemma = tmp[2];
						evnt1Rel = tmp[3];

						evnt2Polarity = true;
						evnt2 = tmp[7];
						evnt2Lemma = tmp[8];
						evnt2Rel = tmp[9];
					}					
				}

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(evnt1);
				root.setLemma(evnt1Lemma);
				root.setSuperclass(evnt1Lemma);
				root.setPostag("vb");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				if(!evnt1Polarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}

				edges.add(evnt1Rel);
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children.add(ch2);
				
				root.setEdges(edges);
				root.setChildren(children);
								
				if(type.equals("action causes action")){
					edges.add("causes");
				}else if (type.equals("action prevents action")){
					edges.add("prevents");
				}
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(evnt2);
				ch3.setLemma(evnt2Lemma);
				ch3.setSuperclass(evnt2Lemma);
				ch3.setPostag("vb");
				children.add(ch3);
				
				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();
				if(!evnt2Polarity){
					edges2.add("negative");
					KnowledgeGraphNode ch12 = new KnowledgeGraphNode();
					ch12.setValue("n2");
					ch12.setLemma("nt");
					ch12.setSuperclass("nt");
					ch12.setPostag("rb");
					children2.add(ch12);
				}

				edges2.add(evnt2Rel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("nn");
				children2.add(ch22);
				
				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 2: " + aspFormatKnow);
			}
			break;

		case "type3": // a property causes an action
			if(tmp.length==5){
				String type = "prop causes action";
				String trait = tmp[1];
				String traitLemma = trait;
				if(trait.matches("(.*)(-)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("-"));
				}else if(trait.matches("(.*)(_)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("_"));
				}
				boolean traitPolarity = false;
				if(tmp[0].equals("positive")){
					traitPolarity = true;
				}

				String event = tmp[2];
				String eventLemma = tmp[3];
				String eventRel = tmp[4];

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(trait);
				root.setLemma(traitLemma);
				root.setSuperclass(traitLemma);
				root.setPostag("j");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				if(!traitPolarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}

				edges.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children.add(ch2);

				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("causes");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(event);
				ch3.setLemma(eventLemma);
				ch3.setSuperclass(eventLemma);
				ch3.setPostag("vb");
				children.add(ch3);
				
				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();

				edges2.add(eventRel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("nn");
				children2.add(ch22);

				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 3: " + aspFormatKnow);
			}

			break;

		case "type4": // an action causes a property
			if(tmp.length==6){
				String type = "action causes prop";
				
				boolean eventPolarity = false;
				if(tmp[2].equals("positive")){
					eventPolarity = true;
				}
				String event = tmp[3];
				String eventLemma = tmp[4];
				String eventRel = tmp[5];

				String trait = tmp[1];
				String traitLemma = trait;
				if(trait.matches("(.*)(-)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("-"));
				}
				if(trait.matches("(.*)(_)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("_"));
				}
				boolean traitPolarity = false;
				if(tmp[0].equals("positive")){
					traitPolarity = true;
				}
				
				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(event);
				root.setLemma(eventLemma);
				root.setSuperclass(eventLemma);
				root.setPostag("vb");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				edges.add(eventRel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("nn");
				children.add(ch22);
				
				if(!eventPolarity){
					edges.add("negative");
					KnowledgeGraphNode ch1neg = new KnowledgeGraphNode();
					ch1neg.setValue("n1");
					ch1neg.setLemma("nt");
					ch1neg.setSuperclass("nt");
					ch1neg.setPostag("r");
					children.add(ch1neg);
				}
				
				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("causes");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(trait);
				ch3.setLemma(traitLemma);
				ch3.setSuperclass(traitLemma);
				ch3.setPostag("jj");
				children.add(ch3);
				
				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();

				edges2.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children2.add(ch2);
				
				if(!traitPolarity){
					edges2.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n2");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children2.add(ch1);
				}

				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 4: " + aspFormatKnow);
			}

			break;

		case "type5":
			break;

		case "type6": // an action is followed by another action
			if(tmp.length==11){
				String type = "action followed by action";
				String evnt1 = null;
				String evnt1Lemma = null;
				String evnt1Rel = null;
				boolean evnt1Polarity = false;

				String evnt2 = null;
				String evnt2Lemma = null;
				String evnt2Rel = null;
				boolean evnt2Polarity = false;

				if(tmp[0].equals("positive") && tmp[6].equals("positive")){
					evnt1 = tmp[1];
					evnt1Polarity = true;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = true;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}else if(tmp[0].equals("positive") && tmp[6].equals("negative")){
					evnt1 = tmp[1];
					evnt1Polarity = true;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = false;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}else if(tmp[0].equals("negative") && tmp[6].equals("positive")){
					evnt1 = tmp[1];
					evnt1Polarity = false;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = true;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}else if(tmp[0].equals("negative") && tmp[6].equals("negative")){
					evnt1 = tmp[1];
					evnt1Polarity = false;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = false;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(evnt1);
				root.setLemma(evnt1Lemma);
				root.setSuperclass(evnt1Lemma);
				root.setPostag("vb");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				if(!evnt1Polarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}

				edges.add(evnt1Rel);
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children.add(ch2);

				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("followed_by");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(evnt2);
				ch3.setLemma(evnt2Lemma);
				ch3.setSuperclass(evnt2Lemma);
				ch3.setPostag("vb");
				children.add(ch3);
				
				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();
				if(!evnt2Polarity){
					edges2.add("negative");
					KnowledgeGraphNode ch12 = new KnowledgeGraphNode();
					ch12.setValue("n2");
					ch12.setLemma("nt");
					ch12.setSuperclass("nt");
					ch12.setPostag("rb");
					children2.add(ch12);
				}

				edges2.add(evnt2Rel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("nn");
				children2.add(ch22);

				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 6: " + aspFormatKnow);
			}
			break;

		case "type7": // an action followed by a property
			if(tmp.length==6){
				String type = "action followed by prop";
				
				boolean eventPolarity = false;
				if(tmp[0].equals("positive")){
					eventPolarity = true;
				}
				String event = tmp[1];
				String eventLemma = tmp[2];
				String eventRel = tmp[3];

				String trait = tmp[5];
				String traitLemma = trait;
				if(trait.matches("(.*)(-)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("-"));
				}
				if(trait.matches("(.*)(_)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("_"));
				}
				boolean traitPolarity = false;
				if(tmp[4].equals("positive")){
					traitPolarity = true;
				}
				
				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(event);
				root.setLemma(eventLemma);
				root.setSuperclass(eventLemma);
				root.setPostag("v");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				
				if(!eventPolarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}
				
				edges.add(eventRel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("n");
				children.add(ch22);
				
				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("followed_by");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(trait);
				ch3.setLemma(traitLemma);
				ch3.setSuperclass(traitLemma);
				ch3.setPostag("jj");
				children.add(ch3);
				
				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();

				edges2.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children2.add(ch2);
				
				if(!traitPolarity){
					edges2.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n2");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children2.add(ch1);
				}

				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 7: " + aspFormatKnow);
			}

			break;

		case "type8": // a property followed by an action
			if(tmp.length==6){
				String type = "prop followed by action";
				
				boolean traitPolarity = false;
				if(tmp[0].equals("positive")){
					traitPolarity = true;
				}
				String trait = tmp[1];
				String traitLemma = trait;
				if(trait.matches("(.*)(-)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("-"));
				}
				if(trait.matches("(.*)(_)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("_"));
				}
				
				boolean eventPolarity = false;
				if(tmp[2].equals("positive")){
					eventPolarity = true;
				}
				String event = tmp[3];
				String eventLemma = tmp[4];
				String eventRel = tmp[5];

				
				
				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(trait);
				root.setLemma(traitLemma);
				root.setSuperclass(traitLemma);
				root.setPostag("jj");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				
				if(!traitPolarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}
				
				edges.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children.add(ch2);
				
				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("followed_by");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(event);
				ch3.setLemma(eventLemma);
				ch3.setSuperclass(eventLemma);
				ch3.setPostag("v");
				children.add(ch3);
				
				ArrayList<String> edges3 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children3 = new ArrayList<KnowledgeGraphNode>();
				if(!eventPolarity){
					edges3.add("negative");
					KnowledgeGraphNode ch31 = new KnowledgeGraphNode();
					ch31.setValue("n1");
					ch31.setLemma("nt");
					ch31.setSuperclass("nt");
					ch31.setPostag("rb");
					children3.add(ch31);
				}
				
				edges3.add(eventRel);
				KnowledgeGraphNode ch32 = new KnowledgeGraphNode();
				ch32.setValue("x");
				//				ch22.setLemma("");
				ch32.setSuperclass("entity");
				ch32.setPostag("n");
				children3.add(ch32);
				
				ch3.setEdges(edges3);
				ch3.setChildren(children3);
				
				root.setEdges(edges);
				root.setChildren(children);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 8: " + aspFormatKnow);
			}

			break;

			//			Below code covers the case when an action and a property occur together. In other words there is a correlation between an action and a property
		case "type9_1": // an action and a property occur together
			if(tmp.length==6){
				String type = "action and prop";
				String event = tmp[1];
				String eventLemma = tmp[2];
				String eventRel = tmp[3];
				boolean eventPolarity = false;
				if(tmp[0].equals("positive")){
					eventPolarity = true;
				}

				String trait = tmp[5];
				String traitLemma = trait;
				if(trait.matches("(.*)(-)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("-"));
				}
				if(trait.matches("(.*)(_)([0-9]{1,7})")){	
					traitLemma = trait.substring(0, trait.lastIndexOf("_"));
				}
				boolean traitPolarity = false;
				if(tmp[4].equals("positive")){
					traitPolarity = true;
				}

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(event);
				root.setLemma(eventLemma);
				root.setSuperclass(eventLemma);
				root.setPostag("vb");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				edges.add(eventRel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("nn");
				children.add(ch22);

				if(!eventPolarity){
					edges.add("negative");
					KnowledgeGraphNode ch21 = new KnowledgeGraphNode();
					ch21.setValue("n1");
					ch21.setLemma("nt");
					ch21.setSuperclass("nt");
					ch21.setPostag("rb");
					children.add(ch21);
				}

				root.setEdges(edges);
				root.setChildren(children);

				edges.add("and");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(trait);
				ch3.setLemma(traitLemma);
				ch3.setSuperclass(traitLemma);
				ch3.setPostag("jj");
				children.add(ch3);

				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();

				edges2.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children2.add(ch2);

				if(!traitPolarity){
					edges2.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n2");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children2.add(ch1);
				}

				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in generating an object of Knowledge Type Event and Property: " + aspFormatKnow);
			}
			break;
			
			//		Below code covers the case when two actions occur together. In other words there is a correlation between two actions
		case "type9_2": // two actions occur together
			if(tmp.length==11){
				String type = "action and action";
				String evnt1 = null;
				String evnt1Lemma = null;
				String evnt1Rel = null;
				boolean evnt1Polarity = false;

				String evnt2 = null;
				String evnt2Lemma = null;
				String evnt2Rel = null;
				boolean evnt2Polarity = false;

				if(tmp[0].equals("positive") && tmp[6].equals("positive")){
					evnt1 = tmp[1];
					evnt1Polarity = true;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = true;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}else if(tmp[0].equals("positive") && tmp[6].equals("negative")){
					evnt1 = tmp[1];
					evnt1Polarity = true;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = false;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}else if(tmp[0].equals("negative") && tmp[6].equals("positive")){
					evnt1 = tmp[1];
					evnt1Polarity = false;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = true;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}else if(tmp[0].equals("negative") && tmp[6].equals("negative")){
					evnt1 = tmp[1];
					evnt1Polarity = false;
					evnt1Rel = tmp[3];
					evnt1Lemma = tmp[2];

					evnt2 = tmp[7];
					evnt2Polarity = false;
					evnt2Rel = tmp[9];
					evnt2Lemma = tmp[8];
				}

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(evnt1);
				root.setLemma(evnt1Lemma);
				root.setSuperclass(evnt1Lemma);
				root.setPostag("vb");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				if(!evnt1Polarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}

				edges.add(evnt1Rel);
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("nn");
				children.add(ch2);
				
				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("and");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(evnt2);
				ch3.setLemma(evnt2Lemma);
				ch3.setSuperclass(evnt2Lemma);
				ch3.setPostag("vb");
				children.add(ch3);
				
				ArrayList<String> edges2 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children2 = new ArrayList<KnowledgeGraphNode>();
				if(!evnt2Polarity){
					edges2.add("negative");
					KnowledgeGraphNode ch12 = new KnowledgeGraphNode();
					ch12.setValue("n2");
					ch12.setLemma("nt");
					ch12.setSuperclass("nt");
					ch12.setPostag("rb");
					children2.add(ch12);
				}

				edges2.add(evnt2Rel);
				KnowledgeGraphNode ch22 = new KnowledgeGraphNode();
				ch22.setValue("x");
				//				ch22.setLemma("");
				ch22.setSuperclass("entity");
				ch22.setPostag("nn");
				children2.add(ch22);

				ch3.setEdges(edges2);
				ch3.setChildren(children2);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 9_ene: " + aspFormatKnow);
			}
			break;

			//		Below code covers the case when two properties occur together. In other words there is a correlation between two actions
		case "type9_3": // two properties occur together
			if(tmp.length==4){
				
				String type = "prop and prop";
				
				boolean trait1Polarity = false;
				if(tmp[0].equals("positive")){
					trait1Polarity = true;
				}
				String trait1 = tmp[1];
				String trait1Lemma = trait1;
				if(trait1.matches("(.*)(-)([0-9]{1,7})")){	
					trait1Lemma = trait1.substring(0, trait1.lastIndexOf("-"));
				}
				if(trait1.matches("(.*)(_)([0-9]{1,7})")){	
					trait1Lemma = trait1.substring(0, trait1.lastIndexOf("_"));
				}
				
				boolean trait2Polarity = false;
				if(tmp[2].equals("positive")){
					trait2Polarity = true;
				}
				String trait2 = tmp[3];
				String trait2Lemma = trait2;
				if(trait2.matches("(.*)(-)([0-9]{1,7})")){	
					trait2Lemma = trait2.substring(0, trait2.lastIndexOf("-"));
				}
				if(trait2.matches("(.*)(_)([0-9]{1,7})")){	
					trait2Lemma = trait2.substring(0, trait2.lastIndexOf("_"));
				}

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(trait1);
				root.setLemma(trait1Lemma);
				root.setSuperclass(trait1Lemma);
				root.setPostag("j");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				if(!trait1Polarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}
				
				edges.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("n");
				children.add(ch2);
				
				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("and");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(trait2);
				ch3.setLemma(trait2Lemma);
				ch3.setSuperclass(trait2Lemma);
				ch3.setPostag("j");
				children.add(ch3);
				
				ArrayList<String> edges3 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children3 = new ArrayList<KnowledgeGraphNode>();
				if(!trait2Polarity){
					edges3.add("negative");
					KnowledgeGraphNode ch31 = new KnowledgeGraphNode();
					ch31.setValue("n2");
					ch31.setLemma("nt");
					ch31.setSuperclass("nt");
					ch31.setPostag("r");
					children3.add(ch31);
				}

				edges3.add("is_trait_of");
				KnowledgeGraphNode ch32 = new KnowledgeGraphNode();
				ch32.setValue("x");
				//				ch22.setLemma("");
				ch32.setSuperclass("entity");
				ch32.setPostag("n");
				children3.add(ch32);

				ch3.setEdges(edges3);
				ch3.setChildren(children3);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 9_pnp: " + aspFormatKnow);
			}
			break;

		case "type10": // a property causes another property
			if(tmp.length==4){
				
				String type = "prop causes prop";
				
				boolean trait1Polarity = false;
				if(tmp[0].equals("positive")){
					trait1Polarity = true;
				}
				String trait1 = tmp[1];
				String trait1Lemma = trait1;
				if(trait1.matches("(.*)(-)([0-9]{1,7})")){	
					trait1Lemma = trait1.substring(0, trait1.lastIndexOf("-"));
				}
				if(trait1.matches("(.*)(_)([0-9]{1,7})")){	
					trait1Lemma = trait1.substring(0, trait1.lastIndexOf("_"));
				}
				
				boolean trait2Polarity = false;
				if(tmp[2].equals("positive")){
					trait2Polarity = true;
				}
				String trait2 = tmp[3];
				String trait2Lemma = trait2;
				if(trait2.matches("(.*)(-)([0-9]{1,7})")){	
					trait2Lemma = trait2.substring(0, trait2.lastIndexOf("-"));
				}
				if(trait2.matches("(.*)(_)([0-9]{1,7})")){	
					trait2Lemma = trait2.substring(0, trait2.lastIndexOf("_"));
				}

				knowledge = new KnowledgeObject();
				knowledge.setType(type);

				KnowledgeGraphNode root = new KnowledgeGraphNode();
				root.setValue(trait1);
				root.setLemma(trait1Lemma);
				root.setSuperclass(trait1Lemma);
				root.setPostag("j");
				ArrayList<String> edges = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children = new ArrayList<KnowledgeGraphNode>();
				if(!trait1Polarity){
					edges.add("negative");
					KnowledgeGraphNode ch1 = new KnowledgeGraphNode();
					ch1.setValue("n1");
					ch1.setLemma("nt");
					ch1.setSuperclass("nt");
					ch1.setPostag("rb");
					children.add(ch1);
				}
				
				edges.add("is_trait_of");
				KnowledgeGraphNode ch2 = new KnowledgeGraphNode();
				ch2.setValue("x");
				//				ch2.setLemma("");
				ch2.setSuperclass("entity");
				ch2.setPostag("n");
				children.add(ch2);
				
				root.setEdges(edges);
				root.setChildren(children);
				
				edges.add("causes");
				KnowledgeGraphNode ch3 = new KnowledgeGraphNode();
				ch3.setValue(trait2);
				ch3.setLemma(trait2Lemma);
				ch3.setSuperclass(trait2Lemma);
				ch3.setPostag("j");
				children.add(ch3);
				
				ArrayList<String> edges3 = new ArrayList<String>();
				ArrayList<KnowledgeGraphNode> children3 = new ArrayList<KnowledgeGraphNode>();
				if(!trait2Polarity){
					edges3.add("negative");
					KnowledgeGraphNode ch31 = new KnowledgeGraphNode();
					ch31.setValue("n2");
					ch31.setLemma("nt");
					ch31.setSuperclass("nt");
					ch31.setPostag("r");
					children3.add(ch31);
				}

				edges3.add("is_trait_of");
				KnowledgeGraphNode ch32 = new KnowledgeGraphNode();
				ch32.setValue("x");
				//				ch22.setLemma("");
				ch32.setSuperclass("entity");
				ch32.setPostag("n");
				children3.add(ch32);

				ch3.setEdges(edges3);
				ch3.setChildren(children3);
				
				knowledge.setRoot(root);
			}else{
				System.err.println("Error in processing Knowledge Type 10: " + aspFormatKnow);
			}
			break;
		}

		return knowledge;
	}
	
	public String getConnPolarity(String conn){
		conn = conn.toLowerCase();
		if(mapOfConns.containsKey(conn)){
			return mapOfConns.get(conn);
		}
		return "forward";
	}

	private HashMap<String,String> populateMapOfConns(String connsFileName){
		HashMap<String,String> result = new HashMap<String, String>();
		try(BufferedReader br = new BufferedReader(new FileReader(connsFileName))){
			String line = null;
			while((line=br.readLine())!=null){
				if(!line.startsWith("#")){
					String[] tmp = line.trim().split("\t");
					if(tmp.length==2){
						result.put(tmp[0], tmp[1]);
					}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return result;
	}
}
