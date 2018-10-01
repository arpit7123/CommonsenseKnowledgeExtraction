/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import module.graph.helper.GraphPassingNode;

import com.google.common.base.Joiner;

import helper.DiscourseInfo;
import utils.Configurations;

/**
 * @author Arpit Sharma
 *
 */
public class TextPreprocessor {
	private String connsFile = Configurations.getProperty("connsfile");
	private ArrayList<String> connsList = null;
	private int sent_words_limit = 30;
	private Pattern parPat = Pattern.compile("(.*)(\\(.*\\))(.*)");
	private Pattern wordPat = Pattern.compile("(.*)(-)([0-9]{1,7})");
	private String genderFile = null;
	private HashMap<String,String> genderMap = null;
	private static TextPreprocessor preprocessor = null;
	

	static{
		preprocessor = new TextPreprocessor();
	}

	public static TextPreprocessor getInstance(){
		return preprocessor;
	}

	private TextPreprocessor(){
		populateConns();
		genderFile = Configurations.getProperty("genderfile");
		genderMap = populateGenderMap(genderFile);
	}

	public ArrayList<String> breakParagraph(String paragraph, int sent_words_limit){
		this.sent_words_limit = sent_words_limit;
		return breakParagraph(paragraph);
	}

	public ArrayList<String> breakParagraph(String paragraph){
		ArrayList<String> result = new ArrayList<String>();
		String[] words = paragraph.split(" ");
		int finIndx = words.length/sent_words_limit;
		if(words.length%sent_words_limit!=0){
			finIndx +=1; 
		}
		for(int i=0; i<finIndx; ++i){
			if(words.length-1 >= (i*sent_words_limit)+(sent_words_limit-1)){
				result.add(Joiner.on(" ").join(Arrays.copyOfRange(words, i*sent_words_limit, (i*sent_words_limit)+(sent_words_limit-1))));
			}else{
				result.add(Joiner.on(" ").join(Arrays.copyOfRange(words, i*sent_words_limit, words.length-1)));
			}
		}

		//		boolean wrongWords = false;
		//		for(String word : words){
		//			if(word.length() > words_chars_limit){
		//				wrongWords = true;
		//			}
		//		}
		return result;
	}

	public ArrayList<DiscourseInfo> divideUsingDiscConn(String sent){
		ArrayList<DiscourseInfo> discInfoList = new ArrayList<DiscourseInfo>();
		for(String conn : connsList){
			if(!conn.equals(",") && !conn.equals(";")){
				conn = " "+conn+" ";
			}
			String[] partsOfSent = sent.split(conn);
//			if(partsOfSent.length > 1){				
			for(int i=0;i<partsOfSent.length-1;++i){
				DiscourseInfo discInfo = new DiscourseInfo();
				if(conn.equals(" \\. ")){
					conn = ".";
				}
				discInfo.setConn(conn.trim());
				String tmpLeft = Joiner.on(conn).join(Arrays.copyOfRange(partsOfSent, 0, i+1));
				if(tmpLeft.trim().equals("")){
					continue;
				}
				discInfo.setLeftArg(tmpLeft);
				String tmpRight = Joiner.on(conn).join(Arrays.copyOfRange(partsOfSent, i+1, partsOfSent.length));
				if(tmpRight.trim().equals("")){
					continue;
				}
				discInfo.setRightArg(tmpRight);
				discInfoList.add(discInfo);
			}
				//				for(int i = 1; i<=partsOfSent.length-1;++i){
				//					discInfo.setLeftArg(Joiner.on(" ").join(Arrays.copyOfRange(partsOfSent, 0, i)));
				//					discInfo.setRightArg(Joiner.on(" ").join(Arrays.copyOfRange(partsOfSent, i+1, partsOfSent.length)));
				//					discInfoList.add(discInfo);
				//				}
//			}
			//			Pattern regExPat = Pattern.compile("(\\b[I|i]f\\b)?(.*)("+ conn +")(.*)");
			//			Matcher m = regExPat.matcher(sent);
			//			if(m.matches()){
			//				discInfo = new DiscourseInfo();
			//				String part1 = m.group(2);
			//				if(m.group(1)!=null){
			//					part1 = "If "+part1;
			//					discInfo.setInitConn("if");
			//				}
			//				discInfo.setConn(m.group(3));
			//				discInfo.setLeftArg(part1);
			//				discInfo.setRightArg(m.group(4));
			//				discInfoList.add(discInfo);
			//			}
		}
		
//		if(discInfoList.size()==0){
			DiscourseInfo discInfo = new DiscourseInfo();
			discInfo.setLeftArg(sent);
			discInfoList.add(discInfo);
//		}
		return discInfoList;
	}

	public HashMap<String,ArrayList<String>> simpleCoreference(GraphPassingNode gpn){
		HashMap<String,ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		//		inputText = removeParentheses(inputText);
		HashMap<String,String> posMap = gpn.getposMap();
		HashMap<String,HashSet<String>> parentMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> childMap = new HashMap<String, HashSet<String>>();
		ArrayList<String> rdfGraph = gpn.getAspGraph();
		for(String triplet : rdfGraph){
			triplet = triplet.substring(4,triplet.length()-2);
			String[] tmpArr = triplet.split(",");
			if(!tmpArr[1].equalsIgnoreCase("instance_of") 
					&& !tmpArr[1].equalsIgnoreCase("is_subclass_of")
					&& !tmpArr[1].equalsIgnoreCase("semantic_role")){
				if(parentMap.containsKey(tmpArr[2])){
					HashSet<String> parentSet = parentMap.get(tmpArr[2]);
					parentSet.add(tmpArr[0]);
					parentMap.put(tmpArr[2], parentSet);
				}else{
					HashSet<String> parentSet = new HashSet<String>();
					parentSet.add(tmpArr[0]);
					parentMap.put(tmpArr[2], parentSet);
				}

				if(childMap.containsKey(tmpArr[0])){
					HashSet<String> childSet = childMap.get(tmpArr[0]);
					childSet.add(tmpArr[2]);
					childMap.put(tmpArr[0], childSet);
				}else{
					HashSet<String> childSet = new HashSet<String>();
					childSet.add(tmpArr[2]);
					childMap.put(tmpArr[0], childSet);
				}
			}
		}
		
		ArrayList<String> maleNouns = new ArrayList<String>();
		ArrayList<String> neutralNouns = new ArrayList<String>();
		ArrayList<String> femaleNouns = new ArrayList<String>();
		ArrayList<String> selves = new ArrayList<String>();
		ArrayList<String> others = new ArrayList<String>();
		ArrayList<String> malePronouns = new ArrayList<String>();
		ArrayList<String> neutralPronouns = new ArrayList<String>();
		ArrayList<String> femalePronouns = new ArrayList<String>();
		
		ArrayList<String> pluralNouns = new ArrayList<String>();
		ArrayList<String> pluralPronouns = new ArrayList<String>();
		
		ArrayList<String> pluralSelves = new ArrayList<String>();
		
		for(String word : posMap.keySet()){
			if(word==null){
				continue;
			}
//			System.out.println(word);
			Matcher wordMat = wordPat.matcher(word);
			if(wordMat.matches()){
				String jstWord = wordMat.group(1);
				String pos = posMap.get(word);
				if(pos.startsWith("P")||
						pos.startsWith("N")){
					if(isPlural(jstWord)){
						if(pos.startsWith("N")){
							pluralNouns.add(word);
						}else{
							pluralPronouns.add(word);
						}
					}
					
					String[] jstWords = null; 
					if(jstWord.contains("_")){
						jstWords = jstWord.split("_");
					}else{
						jstWords = new String[1];
						jstWords[0] = jstWord;
					}
					
					for(String tmpJstWord : jstWords){
						if(tmpJstWord.equalsIgnoreCase("williams")){
							femaleNouns.add(word);
							maleNouns.add(word);
							continue;
						}
						String gender = getGender(tmpJstWord);
						if(gender.equals("male")){
							if(pos.startsWith("NNP")){
								maleNouns.add(word);
							}else if(pos.startsWith("PRP")){
								malePronouns.add(word);
							}
						}else if(gender.equals("neutral")){
							if(pos.startsWith("NN")){
								neutralNouns.add(word);
							}else if(pos.startsWith("PRP")){
								neutralPronouns.add(word);
							}
						}else if(gender.equals("female")){
							if(pos.startsWith("NNP")){
								femaleNouns.add(word);
							}else if(pos.startsWith("PRP")){
								femalePronouns.add(word);
							}
						}else if(gender.equals("self")){
							selves.add(word);
						}else if(gender.equals("pluralselves")){
							pluralSelves.add(word);
						}else if(gender.equals("other")){
							others.add(word);
						}
						
						break;
					}
				}
			}
		}
		
		
		if(allAreOne(maleNouns)){
			ArrayList<String> males = new ArrayList<String>();
			males.addAll(maleNouns);
			males.addAll(malePronouns);

			int size = males.size();
			if(size > 0){
				boolean ambiguityFlag = false;
				for(int i=0;i<size;++i){
					for(int j=i+1;j<size;++j){
						if(commonParents(parentMap.get(males.get(i)),parentMap.get(males.get(j)))){
							ambiguityFlag = true;
							break;
						}
					}
				}
				if(!ambiguityFlag){
					result.put("males", males);
				}
			}
		}
		
		if(allAreOne(neutralNouns)){
			ArrayList<String> neutrals = new ArrayList<String>();
			neutrals.addAll(neutralNouns);
			neutrals.addAll(neutralPronouns);

			int size = neutrals.size();
			if(size > 0){
				boolean ambiguityFlag = false;
				for(int i=0;i<size;++i){
					for(int j=i+1;j<size;++j){
						if(commonParents(parentMap.get(neutrals.get(i)),parentMap.get(neutrals.get(j)))){
							ambiguityFlag = true;
							break;
						}
					}
				}
				if(!ambiguityFlag){
					result.put("neutrals", neutrals);
				}
			}
		}
		
		
		if(allAreOne(femaleNouns)){
			ArrayList<String> females = new ArrayList<String>();
			females.addAll(femaleNouns);
			females.addAll(femalePronouns);

			int size = females.size();
			if(size > 0){
				boolean ambiguityFlag = false;
				for(int i=0;i<size;++i){
					for(int j=i+1;j<size;++j){
						if(commonParents(parentMap.get(females.get(i)),parentMap.get(females.get(j)))){
							ambiguityFlag = true;
							break;
						}
					}
				}
				if(!ambiguityFlag){
					result.put("females", females);
				}
			}
		}
		
		int size = selves.size();
		if(size > 0){
			boolean ambiguityFlag = false;
			for(int i=0;i<size;++i){
				for(int j=i+1;j<size;++j){
					if(commonParents(parentMap.get(selves.get(i)),parentMap.get(selves.get(j)))){
						ambiguityFlag = true;
						break;
					}
				}
			}
			if(!ambiguityFlag){
				result.put("selves", selves);
			}
		}
		
		size = others.size();
		if(size > 0){
			boolean ambiguityFlag = false;
			for(int i=0;i<size;++i){
				for(int j=i+1;j<size;++j){
					if(commonParents(parentMap.get(others.get(i)),parentMap.get(others.get(j)))){
						ambiguityFlag = true;
						break;
					}
				}
			}
			if(!ambiguityFlag){
				result.put("others", others);
			}
		}
		
		if(allAreOne(pluralNouns)){
			ArrayList<String> plurals = new ArrayList<String>();
			plurals.addAll(pluralNouns);
			plurals.addAll(pluralPronouns);

			size = plurals.size();
			if(size > 0){
				boolean ambiguityFlag = false;
				for(int i=0;i<size;++i){
					for(int j=i+1;j<size;++j){
						if(commonParents(parentMap.get(plurals.get(i)),parentMap.get(plurals.get(j)))){
							ambiguityFlag = true;
							break;
						}
					}
				}
				if(!ambiguityFlag){
					result.put("plurals", plurals);
				}
			}
		}
		
//		if(allAreOne(pluralSelves)){
		size = pluralSelves.size();
		if(size > 0){
			boolean ambiguityFlag = false;
			for(int i=0;i<size;++i){
				for(int j=i+1;j<size;++j){
					if(commonParents(parentMap.get(pluralSelves.get(i)),parentMap.get(pluralSelves.get(j)))){
						ambiguityFlag = true;
						break;
					}
				}
			}
			if(!ambiguityFlag){
				result.put("pluralselves", pluralSelves);
			}
		}
//		}
		
		return result;
	}
	
	
	private boolean isPlural(String word) {
	    if (word.isEmpty()) {
	        return false;
	    }
	    
	    if(word.equalsIgnoreCase("they") 
	    		|| word.equalsIgnoreCase("them")
	    		|| word.equalsIgnoreCase("these")
	    		|| word.equalsIgnoreCase("those")){
	    	return true;
	    }
	    
	    if (word.length()>3 && Character.toUpperCase(word.charAt(word.length()-1)) == 'S') {
	        return true;
	    }

	    return false;
	}
	
	/**
	 * This method compares every two words in a list of words and 
	 * returns true if all the pairs have at-least 90% similar characters (in order).
	 * Otherwise returns false.
	 * @param words
	 * @return
	 */
	private boolean allAreOne(ArrayList<String> words){
		////////////////////////////////
		// Modified to return true in case the input list is empty.
		if(words==null){
			return false;
		}
		if(words.size()==0){
			return true;
		}
		////////////////////////////////
		
		for(int i=0;i<words.size();++i){
			for(int j=i+1;j<words.size();++j){
				String word1 = words.get(i);
				word1 = word1.substring(0, word1.lastIndexOf("-"));
				
				String word2 = words.get(j);
				word2 = word2.substring(0, word2.lastIndexOf("-"));
				
				char[] word1Chars = word1.toCharArray();
				char[] word2Chars = word2.toCharArray();
				int numOfSameChars = 0;
				for(int k=0;k<word1Chars.length;++k){
					if(k < word2Chars.length){
						if(word1Chars[k]==word2Chars[k]){
							numOfSameChars+=1;
						}
					}
				}
				
				int totalChars = word1Chars.length;
				if(totalChars<word2Chars.length){
					totalChars = word2Chars.length;
				}
				
				if((double)(numOfSameChars/totalChars)<0.9){
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean commonParents(HashSet<String> anchorPars, HashSet<String> currentPars){
		if(anchorPars!=null){
			for(String anchorPar : anchorPars){
				if(currentPars!=null){
					for(String currentPar : currentPars){
						if(anchorPar.equals(currentPar)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private String getGender(String word){
		if(word.equalsIgnoreCase("water")){
			return "other";
		}
		
		if(word.equalsIgnoreCase("attack")){
			return "neutral";
		}
		
		if(word.equalsIgnoreCase("truck")){
			return "neutral";
		}
		
		if(word.equalsIgnoreCase("bank")){
			return "neutral";
		}
		
		if(word.equalsIgnoreCase("girl")){
			return "female";
		}
		
		String lowerWord = word.toLowerCase();
		if(genderMap.containsKey(lowerWord)){
				return genderMap.get(lowerWord);
		}else{
			Object gender = null;
			String apiUrl = "https://api.genderize.io/?name="+word;
			try{
				URL url = new URL(apiUrl);
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				String line = null;
				while((line=br.readLine())!=null){
					org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
					org.json.simple.JSONObject obj = (org.json.simple.JSONObject)parser.parse(line);
					gender = obj.get("gender");
				}

			}catch(IOException | org.json.simple.parser.ParseException e){
				gender = "neutral";
				System.err.println("Error Occurred in gender api");
			}
			if(gender!=null){
				return (String)gender;
			}
			return "neutral";
		}
	}
	
	public String removeParentheses(String inputText){
		Matcher m = parPat.matcher(inputText);
		if(m.matches()){
			inputText = m.group(1).trim() + " " + m.group(3).trim();
			inputText = removeParentheses(inputText);
		}
		return inputText;
	}

//	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception{
		TextPreprocessor prep = new TextPreprocessor();
		//		String s = "If something happened then we will take it.";
		//		s = ", came over and gave him a hug, and I knew that he wanted that crown more than anything,” he told WFAA";
		//		System.out.println(s);
		//		ArrayList<DiscourseInfo> info = prep.divideUsingDiscConn(s);
		//		for(DiscourseInfo d : info){
		//			System.out.println(d.toString());
		//		}

//		String testStr = "Arpit (sharma) is trying his hardest ().";
//		testStr = prep.removeParentheses(testStr);
//		System.out.println(testStr);

//		String testWord = "because";
//		String out = prep.getGender(testWord);
//		System.out.println(out);
		
//		String test = "\\.";
//		
//		String sent = "You are so right, I never heard the German Shepherd that attacked and bit me. Since then I am so afraid to walk anywhere I might encounter a dog off leash or on those stupid leases.";
//		for(String conn : prep.connsList){
//			if(conn.equals("\\.")){
//				System.out.println("HELLO");
//			}
//			String[] tmp = sent.split(conn);
//			for(String s : tmp){
//				System.out.println(s);
//			}
//		}
		
		String sent = "She could not lift it off the floor because she is a weak girl";
//		LocalKparser stg = new LocalKparser();
//		GraphPassingNode gpn = stg.extractGraph(sent, false, true);
//		for(String s : gpn.getAspGraph()){
//			System.out.println(s);
//		}
		
		ArrayList<DiscourseInfo> discInfoList = prep.divideUsingDiscConn(sent);
		for(DiscourseInfo di : discInfoList){
			System.out.println(di.toString());
		}
		
		for(int i=0;i<-1;i++){
			System.out.println(i);
		}
		
		System.out.println("NO");
		
		//		Pattern regExPat = Pattern.compile("(\\b[I|i]f\\b)?(.*)("+ "because" +")(.*)");
		//		Matcher m = regExPat.matcher(s);
		//		if(m.matches()){
		//			if(m.group(1)==null){
		//				System.out.println("group1 is null");
		//			}else{
		//				System.out.println("group1: "+ m.group(1));
		//			}
		//			System.out.println("group2: "+m.group(2));
		//			System.out.println("group3: "+m.group(3));
		//			System.out.println("group4: "+m.group(4));
		//		}
		System.exit(0);
	}

	private void populateConns(){
		connsList = new ArrayList<String>();
		try(BufferedReader br = new BufferedReader(new FileReader(connsFile))){
			String line = null;
			while((line=br.readLine())!=null){
				if(!line.startsWith("#")){
					connsList.add(line);					
				}
			}
		}catch(IOException e){
			System.err.println("Error in reading the conns file for knowledge extraction!");
		}
	}
	
	private HashMap<String,String> populateGenderMap(String genderFile){
		HashMap<String,String> genderMap = new HashMap<String, String>();
		try(BufferedReader br = new BufferedReader(new FileReader(genderFile))){
			String line = null;
			while((line=br.readLine())!=null){
				if(!line.startsWith("#")){
					String[] tmpStr = line.split("\t");
					if(tmpStr.length==2){
						genderMap.put(tmpStr[0], tmpStr[1]);
					}
				}
			}
		}catch(IOException e){
			System.err.println("Error in reading the gender file");
		}
		return genderMap;
	}

}
