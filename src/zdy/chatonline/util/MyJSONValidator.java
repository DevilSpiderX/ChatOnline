package zdy.chatonline.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 用于判断文本是否JSON类型
 *
 * @author <a target="_blank" href="https://github.com/devilspiderx">devilspiderx</a>
 */
public class MyJSONValidator {

    /**
     * 判断文本是否JSON类型
     *
     * @param text 需要判断的文本
     * @return 不是JSON类型，返回JSONType.None；是JSONObject类，返回JSONType.Object；是JSON数组类，返回JSONType.Array
     */
    public static JSONType isJson(String text) {
        Object x = JSON.parse(text);
        if (x instanceof JSONObject) {
            return JSONType.Object;
        } else if (x instanceof JSONArray) {
            return JSONType.Array;
        } else {
            return JSONType.None;
        }
    }
}
