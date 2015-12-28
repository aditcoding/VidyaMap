package vidyamap.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import vidyamap.dao.DatabaseConnector;
import vidyamap.parser.SearchRequest;
import vidyamap.parser.SearchResponse;
import vidyamap.util.Util;

public class MeaningExtractorImpl implements MeaningExtractor {
	static MeaningExtractorImpl m = null;
	private Set<NodeMap<String, Object>> masterSetNodes = new LinkedHashSet<NodeMap<String, Object>>();
	private List<Map<String, Object>> masterListLinks = new ArrayList<Map<String,Object>>();
	private List<String> keywords = new ArrayList<String>();
	private String nodesTable = "";
	private String linksTable = "";
	private static String tablesPropsFile = "/tables.properties";
	private static Properties tableProps = new Properties();
	
	static {
		try {
			tableProps.load(Util.loadFileAsStream(tablesPropsFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MeaningExtractorImpl getInstance(){ 
		if(m != null){
			return m;
		}
		m = new MeaningExtractorImpl();
		return m;
	}
	
	private MeaningExtractorImpl() {
		
	}
	
	
	static void log(Object o) {
		System.out.println(o.toString());
	}

	@Override
	public String extractMeaning(SearchRequest req) {
		//Get keywords and query database
		try {
			String meaning = req.getSearchString();
			String subject = req.getSearchString().split("Subject:")[1];
			setTableNames(subject);
			int numLevels = 2;
			keywords.clear();
			masterSetNodes.clear();
			masterListLinks.clear();
			if(meaning.contains(",:-selectionFromAutoCompleteMenu")){
				String actualText = meaning.split(",:-")[0];
				numLevels = executeQuery(actualText, numLevels);
			} else {
				String [] str = meaning.split(" ");
				for(String s : str){
					numLevels = executeQuery(s, numLevels);
				}
			}
			DatabaseConnector.closeConnection();
			SearchResponse response = new SearchResponse();
			response.addKeywords(keywords);
			return response.convertToJson(masterSetNodes, masterListLinks);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void setTableNames(String subject) {
		nodesTable = subject.toUpperCase() + "_NODES";
		linksTable = subject.toUpperCase() + "_LINKS";
		nodesTable = tableProps.getProperty(nodesTable);
		linksTable = tableProps.getProperty(linksTable);
	}

	private int executeQuery(String meaning, int numLevels) {
		try {
			//Getting nodes
			String query = "SELECT * FROM " + nodesTable + " where name = " + "'"+meaning+"'";
			Set<NodeMap<String, Object>> nodes = DatabaseConnector.executeNodesQuery(query);
			if(nodes.size() > 0){
				for(NodeMap<String, Object> nodeMap : nodes){
					String keyword = (String)nodeMap.get("student_id");
					keywords.add(keyword);
                    keyword = (String)nodeMap.get("group_id");
                    keywords.add(keyword);
				}
				masterSetNodes.addAll(nodes);
				recurse(nodes, --numLevels);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return numLevels;		
	}

	private Set<NodeMap<String, Object>> recurse(Set<NodeMap<String, Object>> nodes, int numLevels) throws Exception {
		if(numLevels >= 0){
			Set<NodeMap<String, Object>> nodesAtLevel = new HashSet<NodeMap<String,Object>>();
			for (NodeMap<String, Object> m1 : nodes) {
				String nodeId = m1.get("id").toString();
				// Getting links
				String query = "SELECT * from " + linksTable + " where source = " + "'"+nodeId+"'";
				List<Map<String, Object>> srcLinks = DatabaseConnector.executeLinksQuery(query);
				masterListLinks.addAll(srcLinks);
				query = "SELECT * from "  + linksTable + " where target = " + "'"+nodeId+"'";
				List<Map<String, Object>> targetLinks = DatabaseConnector.executeLinksQuery(query);
				masterListLinks.addAll(targetLinks);
				for (Map<String, Object> m : srcLinks) {
					String target = m.get("target").toString();
					query = "SELECT * from " + nodesTable + " where id = " + "'"+target+"'";
					nodesAtLevel.addAll(DatabaseConnector.executeNodesQuery(query));
				}
				for (Map<String, Object> m : targetLinks) {
					String source = m.get("source").toString();
					query = "SELECT * from " + nodesTable + " where id = " + "'"+source+"'";
					nodesAtLevel.addAll(DatabaseConnector.executeNodesQuery(query));
				}
			}
			masterSetNodes.addAll(nodesAtLevel);
			return recurse(nodesAtLevel, --numLevels);
		}
		return masterSetNodes;
	}
}
