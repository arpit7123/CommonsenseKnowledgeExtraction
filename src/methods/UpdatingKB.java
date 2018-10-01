package methods;

import helper.KnowledgeGraphNode;
import helper.KnowledgeObject;
import utils.DataBase;

public class UpdatingKB {
	
	private DataBase instance = null;
	
	public UpdatingKB(){
		instance = DataBase.getInstance();
	}
	
	public static void main(String[] args) {
		
	}
	
	public void updateKB(KnowledgeObject kObj){
		String jsonUpdateQuery = getUpdateQuery(kObj);
		instance.updateKB(jsonUpdateQuery);
	}
	
	public String getUpdateQuery(KnowledgeObject kObj){
		String type = kObj.getType();
		KnowledgeGraphNode root = kObj.getRoot();
		
		return null;
	}
	
	
	
	

}
