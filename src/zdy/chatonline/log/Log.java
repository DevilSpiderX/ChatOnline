package zdy.chatonline.log;

import com.alibaba.fastjson.JSONObject;
import zdy.chatonline.Constant;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class Log {
    private static final GarbageCollection gc = new GarbageCollection();

    static {
        gc.start();
    }

    public Log() {
        File directory = new File("Log");
        if (directory.mkdir()) {
            System.out.println("日志目录创建完毕");
        }
    }

    /**
     * 写出日志进文件
     *
     * @param logName 日志名称，一般来说用日期就好
     * @param data    日志内容，要包含Time, ClientMessage, Status, Address, Method, Path, HTTPVersion，可包括Error
     */
    public void writeLog(String logName, JSONObject data) {
        Path logPath = Paths.get("Log", logName + ".log");
        data.put("Time", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)));
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(logPath.toFile(), true), StandardCharsets.UTF_8));
            writer.append("{\r\n");
            for (String key : data.keySet()) {
                if (key.equals("ClientMessage")) {
                    writer.append(key).append(":\r\n");
                    writer.append("(\r\n");
                    writer.append(data.getString(key));
                    writer.append("\r\n");
                    writer.append(")\r\n");
                } else {
                    writer.append(key).append(": ").append(data.getString(key)).append("\r\n");
                }
            }
            writer.append("}\r\n\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class GarbageCollection extends Thread {
        private static final int LOG_MAX_NUMBER = Constant.configs.getInteger("LOG_MAX_NUMBER");

        static {
            if (LOG_MAX_NUMBER <= 0) {
                System.err.println("错误：日志最大数量必须为正整数");
                System.exit(LOG_MAX_NUMBER);
            }
        }

        public GarbageCollection() {
            super("Log's Garbage Collection");
        }

        @Override
        public void run() {
            try {
                work();
                sleep(24 * 3600 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void work() {
            File directory = new File("Log");
            if (!directory.exists()) {
                return;
            }
            String[] logNameList = Objects.requireNonNull(directory.list());
            int disparity = logNameList.length - LOG_MAX_NUMBER;
            if (disparity > 0) {
                for (int i = 0; i < disparity; i++) {
                    new File("Log/" + logNameList[i]).delete();
                }
            }
        }
    }

}
