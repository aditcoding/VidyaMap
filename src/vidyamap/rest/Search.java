package vidyamap.rest;

import java.sql.SQLException;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import vidyamap.core.MeaningExtractor;
import vidyamap.core.MeaningExtractorImpl;
import vidyamap.dao.DatabaseConnector;
//NT added code for tracing
//import vidyamap.logUpdate.insertEntry;
import vidyamap.parser.SearchRequest;
import vidyamap.util.Util;

@Path("/")
public class Search {
	
	private static String tablesPropsFile = "/tables.properties";
	private String tableName = "";
	private static Properties tableProps = new Properties();

	static {
		try {
			tableProps.load(Util.loadFileAsStream(tablesPropsFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setTableName(String tblName) {
		tableName = tableProps.getProperty(tblName.toUpperCase());
	}
	
	@Path("search")
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public String postSearch(String req) {	
		//TODO Need to do wiring
		
		/*ObjectMapper mapper = new ObjectMapper();
		try {
			SearchRequest srchReq = mapper.readValue(req, SearchRequest.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    log("performing search");
		log(req);
		SearchRequest srchReq = new SearchRequest();
		srchReq.setSearchString(req);
		MeaningExtractor m = MeaningExtractorImpl.getInstance();
		String response = m.extractMeaning(srchReq);
		
		// NT - Code to initiate log update code path in DB
        //insertEntry e = insertEntry.getInstance();
        //e.addData(req);
        // NT - Code end
		
        return response;
    }
	
	@Path("autocomplete")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
    public String getNodesForAutoComplete(String subject) {
		log("IN autocomplete");
		tableName = subject.toUpperCase() + "_NODES";
		setTableName(tableName);
		String query = "SELECT name FROM " + tableName;
		String ret = null;
		try {
			ret = DatabaseConnector.getNamesForAutoComplete(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }

	static void log(Object o) {
		System.out.println(o.toString());
	}
}
