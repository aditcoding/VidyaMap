package vidyamap.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import vidyamap.core.NodeMap;
import vidyamap.util.Util;

public class DatabaseConnector {

	private static Connection connection = null;
	private static String dbPropsFile = "/db.properties";

	public static Connection getConnection(String propsFilePath) {
		log("-------- Getting database connection ------------");
		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		Properties dbProps = new Properties();
		try {
			dbProps.load(Util.loadFileAsStream(propsFilePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String driver = dbProps.getProperty("driver");
		String url = dbProps.getProperty("url");
		String username = dbProps.getProperty("username");
		String password = dbProps.getProperty("password");
		log("Database properties:::"+ driver + "," + url + "," + username + "," + password);

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			log("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return null;
		}

		log("MySQL JDBC Driver Registered!");

		try {
			connection = DriverManager.getConnection(url, username, password);

		} catch (SQLException e) {
			log("Connection Failed! Check output console");
			e.printStackTrace();
			return null;
		}

		if (connection != null) {
			log("You made it, take control your database now!");
		} else {
			log("Failed to make connection!");
		}
		return connection;
	}

	// TODO Needs to be modified
	public static List<Map<String, Object>> executeLinksQuery(String query)
			throws SQLException {
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			log("Executing query::" + query);
			rs = statement.executeQuery(query);
			log("Query executed");
			return getLinksFromResultSet(rs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Set<NodeMap<String, Object>> executeNodesQuery(String query)
			throws SQLException {
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			log("Executing query::" + query);
			rs = statement.executeQuery(query);
			log("Query executed");
			return getNodesFromResultSet(rs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String getAllNames(String query)
			throws SQLException {
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			log("Executing query::" + query);
			rs = statement.executeQuery(query);
			log("Query executed");
			StringBuilder sb = new StringBuilder();
			while(rs.next()){
				String name = rs.getObject("name").toString();
				String id = rs.getObject("id").toString();
				sb.append(name + ":");
				sb.append(id + ",");
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static List<Map<String,String>> getEverything(String query)
			throws SQLException {
		Statement statement = null;
		ResultSet rs = null;
		List<Map<String,String>> mapList = new ArrayList<Map<String,String>>();
		try {
			connection = getConnection();
			statement = connection.createStatement();
			log("Executing query::" + query);
			rs = statement.executeQuery(query);
			log("Query executed");
			
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			while(rs.next()){
				Map<String, String> retMap = new HashMap<String, String>();
				for (int i = 1; i <= columnCount; i++) {
					String columnName = metaData.getColumnName(i);
					String value = rs.getObject(i).toString().trim();
					retMap.put(columnName, value);
				}
				mapList.add(retMap);
			}
			return mapList;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mapList;
	}
	
	public static String getNamesForAutoComplete(String query)
			throws SQLException {
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			log("Executing query::" + query);
			rs = statement.executeQuery(query);
			log("Query executed");
			StringBuilder sb = new StringBuilder();
			while(rs.next()){
				String name = rs.getObject("name").toString();
				sb.append(name + ",");
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static List<Map<String, Object>> getLinksFromResultSet(ResultSet resultSet) {
		log("Getting entities from result set");
		List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		try {
			while (resultSet.next()) {
				entities.add(getLinkFromResultSet(resultSet));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entities;
	}
	
	private static Set<NodeMap<String, Object>> getNodesFromResultSet(
			ResultSet resultSet) {
		log("Getting entities from result set");
		Set<NodeMap<String, Object>> entities = new HashSet<NodeMap<String, Object>>();
		try {
			while (resultSet.next()) {
				entities.add(getNodeFromResultSet(resultSet));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entities;
	}
	
	private static NodeMap<String, Object> getNodeFromResultSet(ResultSet resultSet) {
		ResultSetMetaData metaData;
		try {
			metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			NodeMap<String, Object> resultsMap = new NodeMap<String, Object>();
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnName(i);
				Object object = resultSet.getObject(i);
				resultsMap.put(columnName, object.toString().trim());
			}
			return resultsMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Map<String, Object> getLinkFromResultSet(ResultSet resultSet) {
		ResultSetMetaData metaData;
		try {
			metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			Map<String, Object> resultsMap = new HashMap<String, Object>();
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnName(i);
				Object object = resultSet.getObject(i);
				resultsMap.put(columnName, object.toString().trim());
			}
			return resultsMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Connection getConnection() {
		return getConnection(dbPropsFile);
	}

	static void log(Object o) {
		//System.out.println(o.toString());
	}

	public static void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
