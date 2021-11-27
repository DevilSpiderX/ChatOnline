package zdy.chatonline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;
import zdy.chatonline.log.Log;
import zdy.chatonline.sql.MessageRecord;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/websocket/{uid}")
public class COWebSocket {
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    protected static ConcurrentHashMap<String, COWebSocket> webSocketMap = new ConcurrentHashMap<>();
    private final static Log log = new Log();
    private static int counter = 0;
    private Session session;
    private HttpSession httpSession;
    private String uid;

    @OnOpen
    public void onOpen(@PathParam("uid") String uid, Session session) throws IOException {
        COWebSocket oldWebSocket;
        if ((oldWebSocket = webSocketMap.get(uid)) != null) {
            oldWebSocket.close();
        }
        this.session = session;
        this.uid = uid;
        httpSession = MainServlet.getUidSession(uid);
        webSocketMap.put(uid, this);
        addOnlineCount();
        String address = (String) httpSession.getAttribute("address");
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nCOWebSocket信息: " + (counter++) + ".（" + address + "） 客户端" + this.uid
                + "接入");
        appendLog("客户端" + this.uid + "接入");
        //这里要写一块登录发送所有未读信息
    }

    @OnClose
    public void onClose() {
        webSocketMap.remove(uid);
        subOnlineCount();
        String address = (String) httpSession.getAttribute("address");
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nCOWebSocket信息: " + (counter++) + ".（" + address + "） 客户端" + this.uid
                + "退出");
        appendLog("客户端" + this.uid + "退出");
    }

    @OnError
    public void onError(Throwable error) {
        String address = (String) httpSession.getAttribute("address");
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nCOWebSocket信息: " + (counter++) + ".（" + address + "） 客户端" + this.uid
                + "出现错误");
        error.printStackTrace();
        appendLog("客户端" + this.uid + "出现错误");
    }

    @OnMessage
    public void onMessage(String msg) throws IOException {//msg格式为json，必须包含cmd参数
        String address = (String) httpSession.getAttribute("address");
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nCOWebSocket信息: " + (counter++) + ".（" + address + "） 来自客户端" + this.uid
                + "的消息 - " + msg);
        appendLog(msg);

        JSONObject data = JSON.parseObject(msg);
        String cmd = data.getString("cmd");
        if (isLoggedIn()) {
            switch (cmd) {
            /*
                添加好友

                应包含参数：friend_uid
                返回代码：0 成功；1 失败；2 friend_uid参数不存在;
             */
                case "addFriend": {
                    if (!data.containsKey("friend_uid")) {
                        sendMessage("{\"cmd\":\"" + cmd + "\",\"code\": \"2\",\"msg\": \"friend_uid参数不存在\"}");
                        break;
                    }
                    //未完成

                    break;
                }
            /*
                发送信息

                应包含参数：receiver_uid, msg
                返回代码：0 成功；1 失败；2 receiver_uid参数不存在;3 msg参数不存在；4 sender_uid和receiver_uid相等；

                接收者接收的信息中的cmd为acceptMessage
                应包含参数:code, msg, time, sender_uid
             */
                case "sendMessage": {
                    if (!data.containsKey("receiver_uid")) {
                        sendMessage("{\"cmd\":\"" + cmd + "\",\"code\": \"2\",\"msg\": \"receiver_uid参数不存在\"}");
                        break;
                    }
                    if (!data.containsKey("msg")) {
                        sendMessage("{\"cmd\":\"" + cmd + "\",\"code\": \"3\",\"msg\": \"msg参数不存在\"}");
                        break;
                    }
                    String receiver_uid = data.getString("receiver_uid");
                    String message = data.getString("msg");

                    if (receiver_uid.equals(uid)) {
                        sendMessage("{\"cmd\":\"" + cmd + "\"," +
                                "\"code\": \"4\",\"msg\": \"sender_uid和receiver_uid相等\"}");
                        break;
                    }

                    SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
                    MessageRecord msgRcd = new MessageRecord();
                    msgRcd.setMessage(message);
                    msgRcd.setTime(Timestamp.valueOf(LocalDateTime.now()));
                    msgRcd.setSenderUid(uid);
                    msgRcd.setReceiverUid(receiver_uid);

                    COWebSocket socket = COWebSocket.webSocketMap.get(receiver_uid);
                    if (socket != null) {
                        socket.sendMessage("{\"cmd\":\"acceptMessage\",\"code\": \"0\",\"msg\": \"" + message + "\"" +
                                ",\"time\":" + msgRcd.getTime().getTime() + ",\"sender_uid\":\"" + uid + "\"}");
                        msgRcd.setState("1");
                    } else {
                        msgRcd.setState("0");
                    }
                    suidRich.insert(msgRcd);
                    sendMessage("{\"cmd\":\"" + cmd + "\",\"code\": \"0\",\"msg\": \"成功\"}");
                    break;
                }
                default: {
                    sendMessage("{\"cmd\":\"" + cmd + "\",\"code\": \"1\",\"msg\": \"未知指令\"}");
                    break;
                }
            }
        } else {
            sendMessage("{\"cmd\":\"" + cmd + "\",\"code\": \"500\",\"msg\": \"没有权限，请登录\"}");
        }
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);

    }

    public static int getOnlineCount() {
        return onlineCount.get();
    }

    public static void addOnlineCount() {
        onlineCount.incrementAndGet();
    }

    public static void subOnlineCount() {
        onlineCount.decrementAndGet();
    }

    public void close() throws IOException {
        session.close();
    }

    private void appendLog(String msg) {
        JSONObject logData = new JSONObject();
        LocalDateTime time = LocalDateTime.now();
        logData.put("Time", time.format(DateTimeFormatter.ofPattern("E, yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)));
        String address = (String) httpSession.getAttribute("address");
        logData.put("Address", address);
        logData.put("Method", "WebSocket");
        logData.put("Message", msg);
        synchronized (log) {
            log.writeLog(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)), logData);
        }
    }

    public boolean isLoggedIn() {
        return httpSession.getAttribute("loggedIn") != null && (Boolean) httpSession.getAttribute("loggedIn");
    }
}
