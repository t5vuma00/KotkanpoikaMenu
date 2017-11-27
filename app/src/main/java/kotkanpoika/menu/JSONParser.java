package kotkanpoika.menu;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

    public JSONParser() {

    }

    JSONObject getjObj(String websiteUrl) {
        HttpURLConnection urlConnection = null;
        String websiteInfo = "";
        JSONObject jsonObject = null;

        try {
            URL url = new URL(websiteUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            in.close();
            websiteInfo = out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                jsonObject = new JSONObject(websiteInfo);
            } catch (JSONException e) {
                Log.e("JSONParser", e.toString());
            }
        }
        return jsonObject;
    }
}
