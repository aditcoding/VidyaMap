package vidyamap.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import vidyamap.core.NodeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


//TODO Need to figure out the structure of the response
public class SearchResponse {
	
	private StringBuilder ret = new StringBuilder();
	private ObjectMapper mapper = new ObjectMapper();
	
	public String convertToJson(Set<NodeMap<String, Object>> nodes,
			List<Map<String, Object>> links) {
		try {
			String nodesJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodes);
			String linksJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(links);
			String nodesAndLinksJson = "\n\"nodes\":" + nodesJson + ",\n\"links\":" + linksJson + "\n}";
			ret.append(nodesAndLinksJson);
			log(ret.toString());
			return ret.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
	}
	
	public void addKeywords(List<String> keywords) {
		try {
			String keywordsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(keywords);
			ret.append("{\n\"keywords\":" + keywordsJson + ",\n");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	static void log(Object o) {
		//System.out.println(o.toString());
	}

}
