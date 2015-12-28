package vidyamap.rest;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import vidyamap.dao.DatabaseConnector;
import vidyamap.dao.DatabaseConnectorExt;
import vidyamap.util.Util;

@Path("/")
public class RegistrationHandler {

	private String tableName = "";
	private String userType = "";
	private UserHandler userHandler = new UserHandler();
	private static Properties tableProps = Util.getTableProperties(); 

	private void setTableName(String tblName) {
		tableName = tableProps.getProperty(tblName.toUpperCase());
	}
	
	private void setUserType(String usrType) {
		userType = tableProps.getProperty(usrType.toUpperCase() + "_TYPE");
	}
	
	
	@Path("getStudents")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getStudents() {
		log("Getting students");
		setTableName("student");
		
		String query = "SELECT name,id FROM " + tableName;
		String ret = null;
		try {
			ret = DatabaseConnector.getAllNames(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }
	
	@Path("getTerms")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getTerms() {
		log("Getting Terms");
		setTableName("term");
		
		String query = "SELECT name,id FROM " + tableName;
		String ret = null;
		try {
			ret = DatabaseConnector.getAllNames(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }
	
	@Path("getSchools")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getSchools() {
		log("Getting schools");
		setTableName("school");
		
		String query = "SELECT name,id FROM " + tableName;
		String ret = null;
		try {
			ret = DatabaseConnector.getAllNames(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }
	
	@Path("getTeachers")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getTeachers() {
		log("Getting Teachers");
		setTableName("teacher");
		
		String query = "SELECT name,id FROM " + tableName;
		String ret = null;
		try {
			ret = DatabaseConnector.getAllNames(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }
	
	@Path("getClasses")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getClasses() {
		log("Getting classes");
		setTableName("class");
		
		String query = "SELECT name,id FROM " + tableName;
		String ret = null;
		try {
			ret = DatabaseConnector.getAllNames(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }
	
	@Path("school")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regSchool(@FormParam("name") String name,
			@FormParam("description") String desc,
			@FormParam("state_name") String stateName,
			@FormParam("district_id") String districtId) {

		log("In school");
		setTableName("school");

		String query = "INSERT INTO " + tableName + " (name, description, state_name, district_id) VALUES ('"
				+ name
				+ "','"
				+ desc
				+ "','"
				+ stateName
				+ "','"
				+ 0 + "')";
		boolean success = true;

		try {
			success = DatabaseConnectorExt.executeUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (success) {
		    URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
	        return Response.temporaryRedirect(uri).build();
		} else {
		    URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
		}
	}
	
	@Path("admin")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regAdmin(@FormParam("firstname") String firstName,
			@FormParam("lastname") String lastName,
			@FormParam("username") String userName,
			@FormParam("password") String passWord) {

		log("In Admin");
		setTableName("user");
		setUserType("admin");
		int privLevel = Integer.parseInt(tableProps.getProperty("ADMIN_PRIVILEGE_LEVEL"));
		boolean userSuccess = userHandler.signUp(firstName, lastName, userName, passWord, privLevel, null, userType);

		if (userSuccess) {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	@Path("guest")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regGuest(@FormParam("firstname") String firstName,
			@FormParam("lastname") String lastName,
			@FormParam("username") String userName,
			@FormParam("password") String passWord) {

		log("In Guest");
		setTableName("user");
		setUserType("guest");
		int privLevel = Integer.parseInt(tableProps.getProperty("GUEST_PRIVILEGE_LEVEL"));
		boolean userSuccess = userHandler.signUp(firstName, lastName, userName, passWord, privLevel, null, userType);

		if (userSuccess) {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	@Path("researcher")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regResearcher(@FormParam("firstname") String firstName,
			@FormParam("lastname") String lastName,
			@FormParam("username") String userName,
			@FormParam("password") String passWord) {

		log("In Researcher");
		setTableName("user");
		setUserType("researcher");
		int privLevel = Integer.parseInt(tableProps.getProperty("RESEARCHER_PRIVILEGE_LEVEL"));
		boolean userSuccess = userHandler.signUp(firstName, lastName, userName, passWord, privLevel, null, userType);

		if (userSuccess) {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	@Path("teacher")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regTeacher(@FormParam("firstname") String firstName,
			@FormParam("lastname") String lastName,
			@FormParam("description") String desc,
			@FormParam("username") String userName,
			@FormParam("password") String passWord,
			@FormParam("schoolSelect") String schoolId) {

		log("In teacher");
		setTableName("teacher");
		setUserType("teacher");
		int privLevel = Integer.parseInt(tableProps.getProperty("TEACHER_PRIVILEGE_LEVEL"));
		String name = firstName + " " + lastName;
		String query = "INSERT INTO " + tableName + " (name, description, school_id) VALUES ('"
				+ name
				+ "','"
				+ desc
				+ "','"
				+ schoolId
				+ "')";
		boolean success = true;
		boolean userSuccess = userHandler.signUp(firstName, lastName, userName, passWord, privLevel, null, userType);

		try {
			success = DatabaseConnectorExt.executeUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (success && userSuccess) {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	@Path("student")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regStudent(@FormParam("firstname") String firstName,
			@FormParam("lastname") String lastName,
			@FormParam("description") String desc,
			@FormParam("username") String userName,
			@FormParam("password") String passWord,
			@FormParam("schoolSelect") String schoolId, @FormParam("log_consent") String logConsent) {

		log("In student and log consent is " + logConsent);
		setTableName("student");
		setUserType("student");
		int privLevel = Integer.parseInt(tableProps.getProperty("STUDENT_PRIVILEGE_LEVEL"));
		String name = firstName + " " + lastName;
		String query = "INSERT INTO " + tableName + " (name, description, school_id) VALUES ('"
				+ name
				+ "','"
				+ desc
				+ "','"
				+ schoolId
				+ "')";
		
		boolean success = true;
		boolean userSuccess = userHandler.signUp(firstName, lastName, userName, passWord, privLevel, logConsent, userType);
		
		try {
			success = DatabaseConnectorExt.executeUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (success && userSuccess) {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	@Path("term")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regTerm(@FormParam("term") String term) {

		log("In term");
		setTableName("term");
		String query = "INSERT INTO " + tableName + " (name) VALUES ('"
				+ term
				+ "') ON DUPLICATE KEY UPDATE name = VALUES(name)";
		
		/*String query = "INSERT INTO " + tableName + " (name) VALUES ('"
				+ term
				+ "')";*/
		boolean success = true;

		try {
			success = DatabaseConnectorExt.executeUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (success) {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	@Path("class")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regClass(@FormParam("name") String name,
			@FormParam("description") String desc,
			@FormParam("teacherSelect") String teacherId) {

		log("In class");
		setTableName("class");
		String query = "INSERT INTO " + tableName + " (name, description, teacher_id) VALUES ('"
				+ name
				+ "','"
				+ desc
				+ "','"
				+ teacherId
				+ "')";
		boolean success = true;

		try {
			success = DatabaseConnectorExt.executeUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (success) {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	@Path("group")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regGroup(@FormParam("description") String desc,
			@FormParam("termSelect") int termId,
			@FormParam("groupname") String groupName,
			@FormParam("password") String passWord,
			@FormParam("classSelect") int classId, @FormParam("studentSelect") List<String> members, @FormParam("log_consent") String logConsent){

		log("In group and log consent is: " + logConsent);
		setTableName("group");
		setUserType("group");
		int privLevel = Integer.parseInt(tableProps.getProperty("GROUP_PRIVILEGE_LEVEL"));
		String query = "INSERT INTO " + tableName + " (name, description, term_id, class_id) VALUES ('"
				+ groupName
				+ "','"
				+ desc
				+ "','"
				+ termId
				+ "','"
				+ classId
				+ "')";
		boolean success = true;
		boolean userSuccess = userHandler.signUp("group","group", groupName, passWord, privLevel, logConsent, userType);

		try {
			success = DatabaseConnectorExt.executeUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (success && userSuccess) {
			query = "SELECT name,id FROM " + tableName + " where name='" + groupName + "'";;
			int idInt = -1;
			try {
				String ret = DatabaseConnector.getAllNames(query);
				if(ret != null && ret.trim().length() > 0){
					String first = ret.split(",")[0];
					if(first != null && first.trim().length() > 0){
						String id = first.split(":")[1];
						if(id != null && id.trim().length() > 0){
							idInt = Integer.parseInt(id);
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			addMembers(idInt, members, 0);
            URI uri = UriBuilder.fromUri("/VidyaMap/html/signup.html").build();
            return Response.temporaryRedirect(uri).build();
        } else {
            URI uri = UriBuilder.fromUri("/VidyaMap/html/error.html").build();
            return Response.temporaryRedirect(uri).build();
        }
	}
	
	private Response addMembers(int groupId, List<String> members, int isLeader) {
		log("Members::::" + members.size());
		Response resp = null;
		for(String member : members){
			log("Member:::" + member);
			resp = addMember(groupId, member, isLeader);
		}
		return resp;
	}

	private Response addMember(int groupId, String member, int isLeader) {
		log("In group_members");
		setTableName("group_members");
		int studentId = Integer.parseInt(member);
		String query = "INSERT INTO " + tableName + " (group_id, student_id, isleader) VALUES ('"
				+ groupId
				+ "','"
				+ studentId
				+ "','"
				+ isLeader
				+ "')";
		boolean success = true;

		try {
			success = DatabaseConnectorExt.executeUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (success) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
	}

	@Path("group_members")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response regGroupMembers(@FormParam("group_id") String groupId,
			@FormParam("studentSelect") List<String> members,
			@FormParam("isleader") int isLeader) {
		
		return addMembers(Integer.parseInt(groupId), members, isLeader);
		
	}

	static void log(Object o) {
		System.out.println(o.toString());
	}
}
