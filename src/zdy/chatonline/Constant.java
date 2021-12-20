package zdy.chatonline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Constant {
    private static final String configFileName = "config.json";
    public static final int PORT;
    public static final String WEB_DIRECTORY;
    public static final int LOG_MAX_NUMBER;
    public static final JSONArray ERROR_PAGE;

    static {
        System.out.println("获取配置信息中......");
        JSONObject configs = new JSONObject();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFileName),
                    StandardCharsets.UTF_8));
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line).append("\n");
            }
            configs.putAll(JSON.parseObject(strBuilder.toString()));
        } catch (FileNotFoundException e) {
            System.err.println("错误：找不到配置文件config.json");
            e.printStackTrace();
            System.exit(404);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(500);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("获取配置信息成功");
        PORT = configs.getInteger("PORT");
        WEB_DIRECTORY = configs.getString("WEB_DIRECTORY");
        LOG_MAX_NUMBER = configs.getInteger("LOG_MAX_NUMBER");
        ERROR_PAGE = configs.getJSONArray("ERROR_PAGE");
    }
}
