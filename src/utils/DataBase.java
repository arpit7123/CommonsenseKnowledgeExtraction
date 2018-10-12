package utils;

import java.net.UnknownHostException;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DataBase {

	private MongoClient mongoClient = null;
	private MongoDatabase db = null;
	private MongoCollection<Document> mainTable = null;

	private static DataBase instance = null;
	
	static{
		try {
			instance = new DataBase();
		} catch (UnknownHostException e) {
			System.err.println("Error in database connection!");
		}
	}
	
	public static DataBase getInstance(){
		return instance;
	}
		
	/**
	 * 
	 * @throws UnknownHostException
	 */
	private DataBase(String dbName ) throws UnknownHostException{
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(dbName);
		mainTable = db.getCollection("maintable");
	}
	
	private DataBase() throws UnknownHostException{
		this("knetdb");
	}
	
	public void updateKB(Document doc, String text){
//		Document doc1 = Document.parse("{$inc : {\"weight\" : 1}}");
//		mainTable.updateOne(doc, doc1);
		Document doc1 = Document.parse("{$inc : {\"weight\" : 1}, $addToSet : {\"texts\" : \"" + text + "\"}}");
		mainTable.updateOne(doc, doc1);
	}
	
	public void insertInKB(Document doc){
		mainTable.insertOne(doc);
	}
	
	public FindIterable<Document> find(){
		FindIterable<Document> docs = mainTable.find();
		return docs;
	}
	
	public FindIterable<Document> find(String jsonStr){
		BasicDBObject obj = BasicDBObject.parse(jsonStr);
		FindIterable<Document> cursor = mainTable.find(obj);
		return cursor;
	}
	
	public static void main(String[] args) throws UnknownHostException{
		DataBase db = new DataBase();
		
		FindIterable<Document> docs = db.find();
		for (Document doc : docs) {
			System.out.println(doc.toJson());
		}
//		System.out.println("************************************************");
//		String jsonStr = "{\"prop\" : [{\"val\" : \"tiny\"} , {\"lemma\" : \"very small\"}], \"rel\" : \"prevents\", \"act\" : \"fit\", \"rel_act\" : \"recipient\", \"weight\" : 24.0 }";
//		db.updateKB(jsonStr);
//		
//		String jsonStr1 = "{\"rel\" : \"prevents\"}";
//		docs = db.find(jsonStr1);
//		for (Document doc : docs) {
//			System.out.println(doc.toString());
//		}
		
//		String str = "{ \"_id\" : { \"$oid\" : \"5bb2aaef68887c3a6bc80586\" }, \"weight\" : 1.0, \"type\" : \"action and prop\", \"knowledge\" : { \"attachment\" : { \"value\" : \"entity\" }, \"and\" : { \"value\" : \"separate\", \"is_trait_of\" : { \"value\" : \"entity\" } }, \"value\" : \"lead\" } }";
//		db.updateKB(str);
	}
	
}