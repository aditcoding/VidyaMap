package vidyamap.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

public class Util {
	
	public static InputStream loadFileAsStream(String fileName) {
		try{
			log("Loading file as stream");
			InputStream is = Util.class.getResourceAsStream(fileName);
			if(is != null){
				return is;
			} else {
				log("InputStream is null");
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String loadFileAsString(String fileName){
		BufferedReader br = null;
		StringBuilder sb = null;
		try {
			String sCurrentLine;
			sb = new StringBuilder();
			br = new BufferedReader(new FileReader(fileName));
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	public static BufferedReader loadFileAsReader(String fileName){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return br;
	}
	
	public static void writeToFile(String str, String fileName){
		PrintWriter writer;
		try {
			log("Writing to file");
			writer = new PrintWriter(fileName, "UTF-8");
			writer.println(str);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Properties getTableProperties() {
		String tablesPropsFile = "/tables.properties";
		Properties tableProps = new Properties();
		try {
			tableProps.load(Util.loadFileAsStream(tablesPropsFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tableProps;
	}
	
	public static Properties getConfigProperties() {
		String configPropsFile = "/config.properties";
		Properties configProps = new Properties();
		try {
			configProps.load(Util.loadFileAsStream(configPropsFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return configProps;
	}

	static void log(Object o) {
		System.out.println(o.toString());
	}
}
