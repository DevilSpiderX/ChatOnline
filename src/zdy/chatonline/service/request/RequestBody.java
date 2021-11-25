package zdy.chatonline.service.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import zdy.chatonline.lang.Bytes;
import zdy.chatonline.util.JSONType;
import zdy.chatonline.util.MyJSONValidator;

import javax.servlet.http.HttpServletRequest;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 请求体类
 *
 * @author <a target="_blank" href="https://github.com/devilspiderx">devilspiderx</a>
 */
public class RequestBody {
    private Type method;
    private Object attribute;

    public enum Type {
        x_www_form_urlencoded {
            public String toString() {
                return "x-www-form-urlencoded";
            }
        },
        form_data {
            public String toString() {
                return "form-data";
            }
        },
        JSONObject {
            public String toString() {
                return "json";
            }
        },
        JSONArray {
            public String toString() {
                return "json";
            }
        },
        xml {
            public String toString() {
                return "xml";
            }
        },
        None {
            public String toString() {
                return "none";
            }
        }

    }

    public RequestBody() {
        method = Type.None;
        attribute = new JSONObject();
    }

    public RequestBody(String contentType, String bodyMsg) {
        parse(contentType, bodyMsg);
    }

    public RequestBody(String contentType, Bytes bodyBytes) {
        parse(contentType, bodyBytes);
    }

    public RequestBody(HttpServletRequest req) throws IOException {
        Bytes bodyBytes = new Bytes();
        int contentLength = req.getContentLength();
        if (contentLength != -1) {
            int count = 0;
            if (count != contentLength) {
                DataInputStream in = new DataInputStream(req.getInputStream());
                byte[] bytes = new byte[8 * 1024];
                int len;
                while (count < contentLength && (len = in.read(bytes)) != -1) {
                    bodyBytes.append(bytes, 0, len);
                    count += len;
                }
            }
        }
        parse(req.getContentType(), bodyBytes);
    }

    /**
     * 解析请求体
     *
     * @param contentType 请求体的类型，在请求头中获取
     * @param message     请求体的内容
     */
    public void parse(String contentType, String message) {
        if (contentType == null) contentType = "";
        int semIndex = contentType.indexOf(';');
        contentType = semIndex == -1 ? contentType : contentType.substring(0, semIndex);
        switch (contentType) {
            case "application/x-www-form-urlencoded": {
                method = Type.x_www_form_urlencoded;
                JSONObject attribute = new JSONObject();
                try {
                    message = URLDecoder.decode(message, StandardCharsets.UTF_8.name());
                    String[] attributeStrings = message.split("&");
                    for (String attributeString : attributeStrings) {
                        String[] nAv = attributeString.split("=");
                        if (nAv.length != 2) continue;
                        String key = nAv[0];
                        String value = nAv[1];
                        attribute.put(key, value);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                this.attribute = attribute;
                break;
            }
            case "multipart/form-data": {
                method = Type.form_data;
                attribute = message;
                break;
            }
            case "application/json": {
                JSONType type = MyJSONValidator.isJson(message);
                if (type == JSONType.Object) {
                    method = Type.JSONObject;
                    this.attribute = JSON.parseObject(message);
                } else if (type == JSONType.Array) {
                    method = Type.JSONArray;
                    this.attribute = JSONArray.parseArray(message);
                }
                break;
            }
            case "text/xml": {
                method = Type.xml;
                attribute = message;
                break;
            }
            default: {
                method = Type.None;
                attribute = new JSONObject();
                break;
            }
        }
    }

    /**
     * 解析请求体
     *
     * @param contentType 请求体的类型，在请求头中获取
     * @param bodyBytes   请求体的内容，二进制形式
     */
    public void parse(String contentType, Bytes bodyBytes) {
        parse(contentType, bodyBytes.getString());
    }

    /**
     * 获取请求体类型
     *
     * @return 请求体类型
     */
    public Type getMethod() {
        return method;
    }

    /**
     * 获取请求体所有参数名
     *
     * @return 一个数组，包含请求体所有参数名
     */
    public String[] getKeys() {
        switch (method) {
            case x_www_form_urlencoded:
            case JSONObject:
            default: {
                return ((JSONObject) attribute).keySet().toArray(new String[0]);
            }
            case form_data:
            case xml: {
                return new String[]{"String"};
            }
            case JSONArray: {
                int size = ((JSONArray) attribute).size();
                String[] result = new String[size];
                for (int i = 0; i < size; i++) {
                    result[i] = String.valueOf(i);
                }
                return result;
            }
        }
    }

    /**
     * 获取请求体参数
     *
     * @param key 参数名
     * @return 请求体参数
     */
    public Object get(String key) {
        switch (method) {
            case x_www_form_urlencoded:
            case JSONObject:
            default: {
                return ((JSONObject) attribute).get(key);
            }
            case form_data:
            case xml: {
                return attribute;
            }
            case JSONArray: {
                int index = Integer.parseInt(key);
                return ((JSONArray) attribute).get(index);
            }
        }
    }

    /**
     * 获取请求体参数
     *
     * @param index 序号
     * @return 请求体参数
     */
    public Object get(int index) {
        switch (method) {
            case x_www_form_urlencoded:
            case JSONObject:
            default: {
                return ((JSONObject) attribute).get(getKeys()[index]);
            }
            case form_data:
            case xml: {
                return attribute;
            }
            case JSONArray: {
                return ((JSONArray) attribute).get(index);
            }
        }
    }

    /**
     * 获取请求体参数
     *
     * @param key 参数名
     * @return 请求体参数
     */
    public String getString(String key) {
        switch (method) {
            case x_www_form_urlencoded:
            case JSONObject:
            default: {
                return ((JSONObject) attribute).getString(key);
            }
            case form_data:
            case xml: {
                return (String) attribute;
            }
            case JSONArray: {
                int index = Integer.parseInt(key);
                return ((JSONArray) attribute).getString(index);
            }
        }
    }

    /**
     * 判断是否存在该请求体参数名
     *
     * @param key 要判断的参数名
     * @return 如果存在该请求体参数名，则返回true；否则，返回false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean contains(String key) {
        switch (method) {
            case x_www_form_urlencoded:
            case JSONObject:
            default: {
                return ((JSONObject) attribute).containsKey(key);
            }
            case form_data:
            case xml: {
                return false;
            }
            case JSONArray: {
                return ((JSONArray) attribute).contains(key);
            }
        }
    }

    @Override
    public String toString() {
        return attribute.toString();
    }
}

