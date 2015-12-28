package vidyamap.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import vidyamap.util.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestSearchRequest {

	private static String searchUrl = "http://localhost:8080/VidyaMap/search";

	public static void main(String[] args) {

		try {

			URL url = new URL(searchUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			//String input = Util.loadFileAsString("tmp/input.json");
			String input = "plants temperature";
			log(input);
			
			OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
			os.write(input);
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode() + " " + conn.getResponseMessage());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			log("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				log(output);
			}
			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void log(Object o) {
		System.out.println(o.toString());
	}
}
