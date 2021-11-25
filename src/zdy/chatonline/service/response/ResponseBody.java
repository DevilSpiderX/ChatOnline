package zdy.chatonline.service.response;

import zdy.chatonline.lang.Bytes;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ResponseBody {
    private final Bytes value;
    private final HttpServletResponse resp;


    public ResponseBody(HttpServletResponse resp) {
        value = new Bytes();
        this.resp = resp;
    }

    public ResponseBody(HttpServletResponse resp, byte[] bytes) {
        value = new Bytes(bytes);
        this.resp = resp;
    }

    /**
     * 将指定的字节数组添加到响应体里去
     *
     * @param bytes 被添加的字节数组
     */
    public void add(byte[] bytes) {
        value.append(bytes);
        resp.setIntHeader("Content-Length", this.size());
    }

    /**
     * 将指定的字节数组按给定的范围添加到响应体里去
     *
     * @param bytes 被添加的字节数组
     * @param off   偏移量
     * @param len   长度
     */
    public void add(byte[] bytes, int off, int len) {
        value.append(bytes, off, len);
        resp.setIntHeader("Content-Length", this.size());
    }

    /**
     * 将指定的字符串添加到响应体里去
     *
     * @param data 响应体的内容
     */
    public void add(String data) {
        value.append(data.getBytes(StandardCharsets.UTF_8));
        resp.setIntHeader("Content-Length", this.size());
    }

    /**
     * 从输入流中获取响应体内容
     * 然后添加为响应体的值
     *
     * @param in 可以获取到响应体内容的输入流
     */
    public void addFromStream(InputStream in) {
        int oneSize = 8 * 1024;
        BufferedInputStream bufferedIn = null;
        try {
            bufferedIn = new BufferedInputStream(in);
            byte[] data = new byte[oneSize];
            int len;
            do {
                len = bufferedIn.read(data);
                if (len == -1) break;
                value.append(data, 0, len);
            } while (len == oneSize);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedIn != null) {
                try {
                    bufferedIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            resp.setIntHeader("Content-Length", this.size());
        }
    }

    /**
     * 从输入流中获取响应体内容
     * 然后按照范围添加为响应体的值
     *
     * @param in     可以获取到响应体内容的输入流
     * @param offset 偏移量
     * @param length 长度
     */
    public void addFromStream(InputStream in, int offset, int length) {
        int oneSize = 8 * 1024;
        int count = 0;
        BufferedInputStream bufferedIn = null;
        try {
            bufferedIn = new BufferedInputStream(in);
            if (bufferedIn.skip(offset) == offset) {
                byte[] data = new byte[oneSize];
                int len;
                do {
                    len = bufferedIn.read(data);
                    if (len == -1) break;
                    int margin = length - count;
                    if (margin < oneSize) {
                        len = Math.min(margin, len);
                        value.append(data, 0, len);
                        break;
                    }
                    count += len;
                    value.append(data, 0, len);
                } while (len == oneSize);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedIn != null) {
                try {
                    bufferedIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            resp.setIntHeader("Content-Length", this.size());
        }
    }

    /**
     * 清空响应体的数据
     */
    public void clean() {
        value.delete(0, value.length());
    }

    /**
     * 获取响应体的值
     * 返回一个字节数组
     *
     * @return 以字节数组为形式的值
     */
    public byte[] get() {
        return value.toByteArray();
    }

    /**
     * 获取响应体的值
     * 返回一个字节串
     *
     * @return 以字节串为形式的值
     */
    public Bytes getValue() {
        return value;
    }

    /**
     * 获取响应体大小
     *
     * @return 所包含的字节串的长度
     */
    public int size() {
        return value.length();
    }

    public void send() throws IOException {
        OutputStream out = resp.getOutputStream();
        out.write(get());
        out.flush();
    }

}
