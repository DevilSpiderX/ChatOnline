package zdy.chatonline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class Constant {
    public static final String LOCAL_PATH = Paths.get(".").toAbsolutePath().toString();
    private static final String configFileName = "config.json";
    public static JSONObject configs = null;

    static {
        System.out.println("获取配置信息中......");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFileName),
                    StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            configs = JSON.parseObject(sb.toString());
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
    }
}
