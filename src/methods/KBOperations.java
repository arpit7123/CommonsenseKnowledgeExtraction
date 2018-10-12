package methods;

import java.util.ArrayList;
import java.util.List;

import helper.KnowledgeGraphNode;
import helper.KnowledgeObject;

import org.bson.Document;

import com.mongodb.client.FindIterable;

import utils.DataBase;

public class KBOperations {

	private DataBase dbInstance = null;
	
	public KBOperations(){
		dbInstance = DataBase.getInstance();
	}
	
	public void updateKB(KnowledgeObject kObj){
		String type = kObj.getType();
		KnowledgeGraphNode root = kObj.getRoot();
		String text = kObj.getText();
		FindIterable<Document> docs = findKnowledgeInKB(root.getJSONObject().toJSONString());
		if(docs.iterator().hasNext()){
			for(Document doc : docs){
				dbInstance.updateKB(doc,text);
				break;
			}
		}else{
			Document dbEntry = new Document();
			dbEntry.put("type", type);
			dbEntry.put("knowledge", Document.parse(root.getJSONString()));
			dbEntry.put("weight", 1.0);
			List<String> listOfTexts = new ArrayList<String>();
			listOfTexts.add(text);
			dbEntry.put("texts", listOfTexts);
			dbInstance.insertInKB(dbEntry);
		}
	}
	
	public FindIterable<Document> findKnowledgeInKB(String searchStr){
		Document doc = Document.parse(searchStr);
		Document doc1 = new Document();
		doc1.put("knowledge", doc);
		return dbInstance.find(doc1.toJson());
	}
	
	
	public static void main(String[] args) {
		KBOperations kbo = new KBOperations();
		String searchStr = "{ \"agent\" : { \"value\" : \"entity\" }, \"and\" : { \"agent\" : { \"value\" : \"entity\" }, \"value\" : \"help\" }, \"value\" : \"try\" }";
		String newText = "Because of this , and due to Man's World's respect of Diana , Artemis often would receive the cold shoulder from those she tried to help , extending even to her brief time";
		FindIterable<Document> docs = kbo.findKnowledgeInKB(searchStr);
		if(docs.iterator().hasNext()){
			for(Document doc : docs){
				kbo.dbInstance.updateKB(doc,newText);
				break;
			}
		}
		System.exit(0);
		
	}

}
