
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class URLMapper {
    private final String JSON_URL = "data/bookkeeping.json";
    private JSONObject jsonObject;

    public URLMapper() throws Exception {
        JSONParser jsonParser = new JSONParser();
        FileReader fileReader = new FileReader(this.JSON_URL);
        this.jsonObject = (JSONObject) jsonParser.parse(fileReader);
    }

    public String getURL(String path) {
        return (String) this.jsonObject.get(path);
    }

}
