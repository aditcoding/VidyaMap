/* 
 * Project : VidyaMap @ Compass Group, WCER
 * Author : Nidhi Tyagi (NT)
 * email : nityagi@cs.wisc.edu
 */

package vidyamap.parser;

import java.util.List;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogFormat {
    
    private StringBuilder ret = new StringBuilder();
    private ObjectMapper mapper = new ObjectMapper();
    
    public String convertToJson(List<HashMap<String, Object>> LogList) {
        try {
            String LogsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LogList);
            String CompleteJson = "\n\"Logs\":" + LogsJson +"\n}";
            ret.append(CompleteJson);
            log(ret.toString());
            return ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void addUserData(List<String> UserData) {
        try {
            String UserDataJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(UserData);
            ret.append("{\n\"UserData\":" + UserDataJson + ",\n");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    static void log(Object o) {
        System.out.println(o.toString());
    }


}
