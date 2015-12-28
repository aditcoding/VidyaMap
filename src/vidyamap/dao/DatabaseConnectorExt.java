/*
 * Project : VidyaMap @ Compass Group, WCER
 * Author : Nidhi Tyagi (NT)
 * email : nityagi@cs.wisc.edu
 */

package vidyamap.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DatabaseConnectorExt extends DatabaseConnector {
    private static Connection connection = null;

    public static boolean executeUpdateQuery(String query)
            throws SQLException {
        boolean result = true;

        //Set the parameter for data insertion aka query execution
        Statement statement = null;
        
        try {
            //Get the DB connection set
            connection = getConnection();
            
            //create statement and execute the insertion query
            statement = connection.createStatement();
            log("Executing log update query::" + query);
            
            statement.executeUpdate(query);       
            log("Query executed");            
          
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                result = false;
            }
        }        
        return result;
    }
    
    public static List<HashMap<String, Object>> executeLogFetchQuery(String query)
            throws SQLException {
        List<HashMap<String, Object>> result = new ArrayList<HashMap<String,Object>>();

        //Set the parameter for data insertion aka query execution
        Statement statement = null;
        ResultSet rs = null;
        
        try {
            //Get the DB connection set
            connection = getConnection();
            
            //create statement and execute the fetch query
            statement = connection.createStatement();
            log("Executing log fetch query::" + query);            
            rs = statement.executeQuery(query);       
            log("Query executed");
            
            //Format the result into a list
            result = getLogsFromResultSet(rs);
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }        
        return result;
    }
    
    private static List<HashMap<String, Object>> getLogsFromResultSet(
            ResultSet resultSet) {
        log("Get entire log entry set: ");
        List<HashMap<String, Object>> LogList = new ArrayList<HashMap<String, Object>>();
        try {
            while (resultSet.next()) {
                LogList.add(getLogFromResultSet(resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return LogList;
    }
    
    private static HashMap<String, Object> getLogFromResultSet(ResultSet rs) {
        ResultSetMetaData metaData;
        try {
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            HashMap<String, Object> Log = new HashMap<String,Object>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object object = rs.getObject(i);
                Log.put(columnName, object.toString().trim());
            }
            return Log;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }
    
    public static Map<String,String> getUserDetails(String query)
            throws SQLException {
        //Set the parameter for query execution
        Statement statement = null;
        ResultSet rs = null;
        Map<String,String> retMap = new HashMap<String, String>();
        try {
            //Get the DB connection set
            connection = getConnection();
            
            //create statement and execute the fetch query
            statement = connection.createStatement();
            log("Executing user authentication fetch query::" + query);            
            rs = statement.executeQuery(query);       
            log("Query executed");
            while(rs.next()){
            	retMap.put("userName", rs.getObject("login_id").toString().trim());
            	retMap.put("password", rs.getObject("password").toString().trim());
            	retMap.put("firstName", rs.getObject("first_name").toString().trim());
            	retMap.put("lastName", rs.getObject("last_name").toString().trim());
            	retMap.put("userType", rs.getObject("user_type").toString().trim());
            	retMap.put("logConsent", rs.getObject("log_consent").toString().trim());
            	retMap.put("privLevel", rs.getObject("privilege_level").toString().trim());
            	retMap.put("salt", rs.getObject("salt").toString().trim());
            	retMap.put("id", rs.getObject("id").toString().trim());
            }
            
            return retMap;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }        
        return retMap;
    }
    
    public static boolean checkIfUsernameExists(String query)
            throws SQLException {
        //Set the parameter for query execution
        Statement statement = null;
        ResultSet rs = null;
        boolean result = true;
        
        try {
            //Get the DB connection set
            connection = getConnection();
            
            //create statement and execute the fetch query
            statement = connection.createStatement();
            log("Executing username lookup query::" + query);            
            rs = statement.executeQuery(query);       
            log("Query executed");
            
            if(!rs.next()){
                result = false;
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }        
        return result;
    }
    
    static void log(Object o) {
        //System.out.println(o.toString());
    }
}
