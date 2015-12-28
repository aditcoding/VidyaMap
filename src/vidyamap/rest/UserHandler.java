package vidyamap.rest;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import vidyamap.dao.DatabaseConnector;
import vidyamap.dao.DatabaseConnectorExt;
import vidyamap.util.LogUtil;
import vidyamap.util.Util;

@Path("/")
public class UserHandler {

	@Context
	private HttpServletRequest request;
	@Context
	private HttpServletResponse response;
	private static PrintWriter pw = null;
	private static Properties tableProps = Util.getTableProperties();
	
	@Path("log")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String createLog(String reqStr) {
		log("In createLog");
		if(pw != null){
			String term = reqStr.split("querytype:")[0];
			String type = reqStr.split("querytype:")[1];
			pw.println(term + ", " + type + ", " + LogUtil.getTimeOfDay());
			pw.flush();
			return "logged";
		} else {
			log("Can't log interaction as pw is null");
		}
		return "not logged";
	}
	
	@Path("subject")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setSubject(@FormParam("subject") String subject){
		log("subject");
		HttpSession session = request.getSession(false);
		URI uri = UriBuilder.fromUri("/VidyaMap/html/main.html").build();
		if(session != null){
			session.setAttribute("subject", subject);
			LogUtil.logSubject(pw, subject);
			return Response.temporaryRedirect(uri).build();
		} else {
			uri = UriBuilder.fromUri("/VidyaMap/index.html").build();
		    return Response.temporaryRedirect(uri).build();
		}
	}
	
	@Path("setGroupInSession")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setGroupInSession(@FormParam("studentSelect") List<String> members){
		log("setGroupInSession");
		HttpSession session = request.getSession(false);
		URI uri = UriBuilder.fromUri("/VidyaMap/html/session.html").build();
		if(session != null){
			session.setAttribute("presentStudents", members.toArray());
			LogUtil.logGroupMembers(pw,members);
			return Response.temporaryRedirect(uri).build();
		} else {
			uri = UriBuilder.fromUri("/VidyaMap/index.html").build();
		    return Response.temporaryRedirect(uri).build();
		}
	}
	
	@Path("getStudentsInGroup")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getStudentsInGroup() {
		log("Getting students in group");
		String grpName = request.getSession().getAttribute("userName").toString();
		String tableName = tableProps.getProperty("GROUP");
		String query = "SELECT * FROM " + tableName + " WHERE name = '" + grpName + "'";
		List<Map<String,String>> ret = null;
		StringBuilder sb = new StringBuilder();
		try {
			ret = DatabaseConnector.getEverything(query);
			if(ret != null && ret.size() > 0){
				String grpId = ret.get(0).get("id");
				if(grpId != null){
					List<Map<String,String>> grpMembers = getGroupMembers(grpId);
					for(Map<String,String> map : grpMembers){
						String studentId = map.get("student_id");
						if(studentId != null){
							List<Map<String,String>> students = getStudentsInGroup(studentId);
							for(Map<String,String> studentMap : students){
								String id = studentMap.get("id");
								String name = studentMap.get("name");
								sb.append(name).append(":").append(id).append(",");
							}
						} else{
							log("Student id is null");
						}
					}
				} else {
					log("Group Id is null");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
    }
	
	private List<Map<String, String>> getStudentsInGroup(String studentId) throws SQLException {
		String tableName = tableProps.getProperty("STUDENT");
		String query = "SELECT * FROM " + tableName + " WHERE id = '" + studentId + "'";
		return DatabaseConnector.getEverything(query);
	}


	private List<Map<String, String>> getGroupMembers(String grpId) throws SQLException {
		String tableName = tableProps.getProperty("GROUP_MEMBERS");
		String query = "SELECT * FROM " + tableName + " WHERE group_id = '" + grpId + "'";
		return DatabaseConnector.getEverything(query);
	}


	@Path("getSessionAttrs")
	@GET
	public String getSessionAttributes() {
		log("Getting session attrs");
		HttpSession session = request.getSession(false);
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if(session != null){
			Enumeration<String> attrs = session.getAttributeNames();
			while(attrs.hasMoreElements()){
				String attr = attrs.nextElement();
				String value = session.getAttribute(attr).toString();
				sb.append("\"").append(attr).append("\"").append(":").append("\"").append(value).append("\"");
				if(attrs.hasMoreElements()){
					sb.append(",");
				}
			}
			sb.append("}");
			log("Session attrs:" + sb.toString());
		}
	    return sb.toString();
	}

	@Path("signout")
	@GET
	public Response signOut() {
		log("SigningOut");
		HttpSession session = request.getSession(false);
		if(session != null){
			session.invalidate();
		}
		LogUtil.logSignedOut(pw);
		URI uri = UriBuilder.fromUri("/VidyaMap/index.html").build();
	    return Response.temporaryRedirect(uri).build();
	}

	@Path("signin")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response signIn(@FormParam("username") String username,
			@FormParam("password") String password) {
		log("SignIn");
		username = username.trim();
		if (username.trim().length() > 0 && password.trim().length() > 0) {
			log("got in");
			// Query the database with the username provided
			String query = "SELECT * FROM user WHERE login_id='" + username + "'";

			try {
				Map<String,String> retMap = DatabaseConnectorExt.getUserDetails(query);
				URI uri;
				if (retMap.size() > 0) {
					// Authenticate password here
					log("Going to compare passwords");
					if (authenticate(password, fromHex(retMap.get("password")), fromHex(retMap.get("salt")))) {
						log("passwords match");
						populateSession(retMap);
						String userType = request.getSession().getAttribute("userType").toString();
						if (Integer.parseInt(retMap.get("privLevel").toString().trim()) == 5) { // 5 or above privilege level are for admin
							uri = UriBuilder.fromUri("../html/signup.html").build();
						} else {
							Properties tableProps = Util.getTableProperties();
							if(userType.equalsIgnoreCase(tableProps.getProperty("GROUP_TYPE"))){
								uri = UriBuilder.fromUri("/VidyaMap/html/group_present.html").build();
							} else {
								uri = UriBuilder.fromUri("/VidyaMap/html/session.html").build();
							}
						}
						// Initiate logging
						String logConsent = request.getSession().getAttribute("logConsent").toString();
						log("Logging consent::" + logConsent);
						if(logConsent.equalsIgnoreCase("true")){
							String fn = request.getSession().getAttribute("firstName").toString();
							String ln = request.getSession().getAttribute("lastName").toString();
							pw = LogUtil.logInit(userType, username, fn, ln);
						}
						return Response.temporaryRedirect(uri).build();
					} else {
						log("password dont match");
						uri = UriBuilder.fromUri("/VidyaMap/index.html").build();
						return Response.temporaryRedirect(uri).build();
					}
				} else {
					throw new Exception("DB result set is empty");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}

	private void populateSession(Map<String, String> retMap) {
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(20*60);
		try {
			session.setAttribute("userName", retMap.get("userName"));
			session.setAttribute("firstName", retMap.get("firstName"));
			session.setAttribute("lastName", retMap.get("lastName"));
			session.setAttribute("userType", retMap.get("userType"));
			session.setAttribute("id", retMap.get("id"));
			session.setAttribute("privLevel", retMap.get("privLevel"));
			session.setAttribute("logConsent", retMap.get("logConsent"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean signUp(String firstname,
			String lastname,
			String username,
			String password, int priVLevel, String logConsent, String userType) {
		log("SignUp");
		username = username.trim();
		if (firstname.trim().length() > 0 && lastname.trim().length() > 0
				&& username.trim().length() > 0 && password.trim().length() > 0) {
			byte[] salt = new byte[8];
			byte[] encryptedPassword = new byte[160];
			String saltStr = new String();
			String encryptPassStr = new String();
			// Generate the salt for encrypting password
			try {
				salt = generateSalt();
				encryptedPassword = getEncryptedPassword(password, salt);
				saltStr = toHex(salt);
				encryptPassStr = toHex(encryptedPassword);
				// log("sal and password: "+saltStr+" "+encryptPassStr);

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			int logCnsnt = 0;
			if(logConsent != null && logConsent.equalsIgnoreCase("yes")){
				logCnsnt = 1;
			}
			// Insert new user information into the database
			// user_type is currently going to hold the salt for us
			String query = "INSERT INTO user (first_name, last_name, login_id, salt, password, privilege_level, log_consent, user_type) VALUES ('"
					+ firstname
					+ "','"
					+ lastname
					+ "','"
					+ username
					+ "','"
					+ saltStr + "','" + encryptPassStr + "','" + priVLevel + "','" + logCnsnt + "','" + userType +"')";
			boolean success = true;

			try {
				success = DatabaseConnectorExt.executeUpdateQuery(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (success) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Path("username")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean checkUserNameExists(String username) {

		boolean success = true;
		log("finding username: " + username);

		// Insert new user information into the database
		String query = "SELECT login_id FROM user WHERE login_id='" + username
				+ "'";

		try {
			success = DatabaseConnectorExt.checkIfUsernameExists(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (success)
			log("success is true");
		return success;
	}
	
	// Password handling functions
	private byte[] generateSalt() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		return salt;
	}

	public byte[] getEncryptedPassword(String password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String algorithm = "PBKDF2WithHmacSHA1";
		int derivedKeyLength = 160;
		int iterations = 20000;

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations,
				derivedKeyLength);
		SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
		return f.generateSecret(spec).getEncoded();
	}

	public boolean authenticate(String attemptedPassword,
			byte[] encryptedPassword, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] encryptedAttemptedPassword = getEncryptedPassword(
				attemptedPassword, salt);
		// log("trying authentication compare");
		return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
	}

	private static byte[] fromHex(String hex) {
		byte[] binary = new byte[hex.length() / 2];
		for (int i = 0; i < binary.length; i++) {
			binary[i] = (byte) Integer.parseInt(
					hex.substring(2 * i, 2 * i + 2), 16);
		}
		return binary;
	}

	private static String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0)
			return String.format("%0" + paddingLength + "d", 0) + hex;
		else
			return hex;
	}

	static void log(Object o) {
		System.out.println(o.toString());
	}
}
