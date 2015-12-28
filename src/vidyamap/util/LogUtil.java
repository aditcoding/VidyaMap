package vidyamap.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class LogUtil {
	
	private static Properties configProps = Util.getConfigProperties();
	private static Properties tableProps = Util.getTableProperties();
	private static PrintWriter pw = null;
	
	public static PrintWriter logInit(String userType, String userName, String fn, String ln) {
		log("In logInit");
		File logFile = null;
		File rootDir = getRootDirectory();
		if(rootDir != null && rootDir.exists()){
			if(userType.equalsIgnoreCase(tableProps.getProperty("GROUP_TYPE"))){
				File groupDir = getDirectory(rootDir,configProps.getProperty("prod.logfiles.groupLogsDirName"));
				pw = initiateLogging(groupDir, userName, fn, ln, logFile, pw);
			} else if(userType.equalsIgnoreCase(tableProps.getProperty("STUDENT_TYPE"))){
				File studDir = getDirectory(rootDir,configProps.getProperty("prod.logfiles.studentLogsDirName"));
				pw = initiateLogging(studDir, userName, fn, ln, logFile, pw);
			}
		} else {
			log("Something went wrong");
			return null;
		}
		return pw;
	}
	
	private static PrintWriter initiateLogging(File whichDir, String userName, String fn, String ln, File logFile, PrintWriter pw) {
		File userSpecificDir = getDirectory(whichDir, userName);
		String day = getDay();
		File dayDir = getDirectory(userSpecificDir, day);
		String timeOfDay = getTimeOfDay();
		String fileName = timeOfDay.replace(":", "-") + ".csv";
		logFile = new File(dayDir, fileName);
		try {
			if(logFile.createNewFile()) {
				pw = new PrintWriter(logFile);
				String firstLine = "# Login Id: " + userName + ", First name: " + fn + ", Last name: " + ln
						 + ", Login time: " + timeOfDay +  " " + Calendar.getInstance().getTimeZone().getDisplayName();
				pw.println(firstLine);
				pw.flush();
				return pw;
			} else {
				log("Creating new log file " + fileName + " failed");
				return null;
			}
		} catch (IOException e) {
			pw.close();
			e.printStackTrace();
		}
		return pw;
	}

	public static String getTimeOfDay() {
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
	    String strTime = sdf.format(cal.getTime());
		return strTime;
	}

	public static String getDay() {
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
	    String strDate = sdf.format(cal.getTime());
	    return strDate;
	}

	public static File getDirectory(File rootDir, final String whichDir) {
		FilenameFilter textFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.equals(whichDir)) {
					return true;
				} else {
					return false;
				}
			}
		};
		File [] dirs = rootDir.listFiles(textFilter);
		if(dirs.length == 0){
			File dir = new File(rootDir, whichDir);
			if(dir.mkdir()){
				log("Creating " + whichDir + " directory");
				return dir;
			} else {
				log("Something went wrong");
				return null;
			}
		} else {
			File dir = dirs[0];
			if(dir.isDirectory() && dir.getName().equals(whichDir)){
				log("Directory " + whichDir + " exists");
				return dir;
			} else {
				log("Something went wrong in creating directory : " + whichDir);
				return null;
			}
		}
	}

	public static File getRootDirectory() {
		log("In checkDirectories");
		String logsLocation = configProps.getProperty("prod.logfiles.root");
		File locationDir = new File(logsLocation);
		if(!locationDir.exists()){
			log("Root direciry does not exist. Create one and set appropriate permissions");
			return null;
		}
		File vidyaMapRootDir = new File(logsLocation, configProps.getProperty("prod.logfiles.vidyaMapLogsDirName"));
		if(!vidyaMapRootDir.exists()){
			log("Creating VidyaMapLogs directory");
			boolean isCreated = vidyaMapRootDir.mkdir();
			if(isCreated){
				log("VidyaMapLogs directory created");
				return vidyaMapRootDir;
			} else {
				log("VidyaMapLogs directory not created. Something is wrong.");
				return null;
			}
		} else {
			log("VidyaMapLogs directory exists already");
			return vidyaMapRootDir;
		}
	}
	
	public static void logSubject(PrintWriter pw, String subject) {
		if(pw != null){
			pw.println("# Subject: " + subject);
			pw.println("# Query term, Query type, Timestamp");
			pw.flush();
		} else {
			log("Can't log subject as pw is null");
		}
	}
	
	public static void logSignedOut(PrintWriter pw) {
		if(pw != null){
			pw.println("# Signed out at: " + LogUtil.getTimeOfDay() + " " + Calendar.getInstance().getTimeZone().getDisplayName());
			pw.flush();
			pw.close();
		} else {
			log("Can't log Logout event as pw is null");
		}
	}
	
	static void log(Object o) {
		System.out.println(o.toString());
	}

	public static void logGroupMembers(PrintWriter pw, List<String> members) {
		if(pw != null){
			StringBuilder sb = new StringBuilder();
			int numberOfMembers = members.size();
			for(int i=0;i<numberOfMembers;i++){
				sb.append(members.get(i));
				if(i != numberOfMembers-1){
					sb.append(", ");
				}
			}
			pw.println("# Members present: " + sb.toString());
			pw.flush();
		} else {
			log("Can't log group members as pw is null");
		}
	}

}