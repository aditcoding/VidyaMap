package vidyamap.parser;

import java.util.HashMap;
import java.util.Map;

//TODO Need to figure out the structure of the request
public class SearchRequest {
    
	private Map<String, String> attrs = new HashMap<String, String>();
	
	
	private String searchString = null;
	
	public Map<String, String> getAttrs() {
		return attrs;
	}




	public void setAttrs(Map<String, String> attrs) {
		this.attrs = attrs;
	}




	public String getSearchString() {
		return searchString;
	}




	public void setSearchString(String srchString) {
		this.searchString = srchString;
	}




	static void log(Object o) {
		System.out.println(o.toString());
	}
}
