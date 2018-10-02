package methods;

import helper.KnowledgeGraphNode;
import helper.KnowledgeObject;

import org.bson.Document;

import com.mongodb.client.FindIterable;

import utils.DataBase;

public class KBOperations {

	private DataBase instance = null;
	
	public KBOperations(){
		instance = DataBase.getInstance();
	}
	
	public void updateKB(KnowledgeObject kObj){
		String type = kObj.getType();
		KnowledgeGraphNode root = kObj.getRoot();
		String text = kObj.getText();
		FindIterable<Document> docs = findKnowledgeInKB(root.getJSONObject().toJSONString());
		if(docs.iterator().hasNext()){
			for(Document doc : docs){
				instance.updateKB(doc);
				break;
			}
		}else{
			Document dbEntry = new Document();
			dbEntry.put("type", type);
			dbEntry.put("knowledge", root.getJSONObject());
			dbEntry.put("weight", 1.0);
			dbEntry.put("text", text);
			instance.insertInKB(dbEntry);
			
		}
	}
	
	public FindIterable<Document> findKnowledgeInKB(String searchStr){
		Document doc = Document.parse(searchStr);
		Document doc1 = new Document();
		doc1.put("knowledge", doc);
		return instance.find(doc1.toJson());
	}
	
	
	public static void main(String[] args) {

	}

}
