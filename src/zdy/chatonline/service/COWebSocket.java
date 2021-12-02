package zdy.chatonline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.teasoft.bee.osql.Condition;
import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.ConditionImpl;
import zdy.chatonline.log.Log;
import zdy.chatonline.sql.Friends;
import zdy.chatonline.sql.MessageRecord;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/websocket/{uid}/{token}")
public class COWebSocket {
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    protected static ConcurrentHashMap<String, COWebSocket> webSocketMap = new ConcurrentHashMap<>();
    private final static Log log = new Log();
    private static int counter = 0;
    private Session session = null;
    private String uid;
    private HttpSession httpSession;
    private String address = "0.0.0.0:0";
    private SendMessageThread sendThread;

    @OnOpen
    public void onOpen(@PathParam("uid") String uid, @PathParam("token") String token, Session session)
            throws IOException {
        COWebSocket oldWebSocket;
        if ((oldWebSocket = webSocketMap.get(uid)) != null) {
            /*
                被挤出在线状态，强制下线

                cmd为forcedOffline
                应包含参数:code, msg
             */
            JSONObject offlineJson = new JSONObject();
            offlineJson.put("cmd", "forcedOffline");
            offlineJson.put("code", "0");
            offlineJson.put("msg", "您的账号在别的客户端登录，您将被强制下线");
            oldWebSocket.sendMessage(offlineJson.toJSONString());

            oldWebSocket.close();
        }
        this.session = session;
        this.uid = uid;
        httpSession = MainServlet.getUidSessionToWS(uid, token);
        webSocketMap.put(uid, this);
        addOnlineCount();
        sendThread = new SendMessageThread(session.getId());
        sendThread.start();
        if (httpSession == null) {
            print(address, "客户端" + this.uid + "非法登录");
            appendLog("客户端" + this.uid + "非法登录");
            this.close();
            return;
        }

        address = (String) httpSession.getAttribute("address");
        print(address, "客户端" + this.uid + "接入");
        print(address, "当前在线用户数为：" + getOnlineCount());
        appendLog("客户端" + this.uid + "接入");
        /*
            接收者接收的信息中的cmd为acceptMessage
            应包含参数:code, msg, time, sender_uid
         */
        SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
        Condition con = new ConditionImpl();
        con.op("state", Op.equal, "0").and().op("receiver_uid", Op.equal, uid);
        List<MessageRecord> msgLists = suidRich.select(new MessageRecord(), con);
        if (!msgLists.isEmpty()) {
            JSONObject acceptMsgJson = new JSONObject();
            for (MessageRecord msgR : msgLists) {
                acceptMsgJson.put("cmd", "acceptMessage");
                acceptMsgJson.put("code", "0");
                acceptMsgJson.put("msg", msgR.getMessage());
                acceptMsgJson.put("time", msgR.getTime().getTime());
                acceptMsgJson.put("sender_uid", msgR.getSenderUid());
                sendMessage(acceptMsgJson.toJSONString());
                acceptMsgJson.clear();
            }
            print(address, "发送未读消息共" + msgLists.size() + "条给客户端" + this.uid);
            for (MessageRecord msgR : msgLists) {
                msgR.setState("1");
                suidRich.update(msgR, "state");
            }
        }

    }

    @OnClose
    public void onClose(CloseReason reason) {
        webSocketMap.remove(uid);
        subOnlineCount();
        print(address, "客户端" + this.uid + "退出");
        print(address, "当前在线用户数为：" + getOnlineCount());
        appendLog("客户端" + this.uid + "退出");
    }

    @OnError
    public void onError(Throwable error) {
        print(address, "客户端" + this.uid + "出现错误");
        error.printStackTrace();
        appendLog("客户端" + this.uid + "出现错误");
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
        sendThread.interrupt();
        session.close();
    }

    @OnMessage
    public void onMessage(String msg) {//msg格式为json，必须包含cmd参数
        print(address, "来自客户端" + this.uid + "的消息 - " + msg);
        appendLog(msg);

        JSONObject data = JSON.parseObject(msg);
        String cmd = data.getString("cmd");
        if (isLoggedIn()) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (cmd) {
            /*
                发送信息

                应包含参数：cmd, receiver_uid, msg
                返回代码：0 成功；1 失败；2 receiver_uid参数不存在;3 msg参数不存在；4 sender_uid和receiver_uid相等；
                        5 不是好友

                接收者接收的信息中的cmd为acceptMessage
                应包含参数:code, msg, time, sender_uid
             */
                case "sendMessage": {
                    JSONObject respJson = new JSONObject();
                    if (!data.containsKey("receiver_uid")) {
                        respJson.put("cmd", cmd);
                        respJson.put("code", "2");
                        respJson.put("msg", "receiver_uid参数不存在");
                        sendMessage(respJson.toJSONString());
                        break;
                    }
                    if (!data.containsKey("msg")) {
                        respJson.put("cmd", cmd);
                        respJson.put("code", "3");
                        respJson.put("msg", "msg参数不存在");
                        sendMessage(respJson.toJSONString());
                        break;
                    }
                    String receiver_uid = data.getString("receiver_uid");
                    String message = data.getString("msg");

                    if (receiver_uid.equals(uid)) {
                        respJson.put("cmd", cmd);
                        respJson.put("code", "4");
                        respJson.put("msg", "sender_uid和receiver_uid相等");
                        sendMessage(respJson.toJSONString());
                        break;
                    }

                    SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
                    if (!suidRich.exist(new Friends(uid, receiver_uid))) {
                        respJson.put("cmd", cmd);
                        respJson.put("code", "5");
                        respJson.put("msg", "你和" + receiver_uid + "不是好友");
                        sendMessage(respJson.toJSONString());
                        break;
                    }

                    MessageRecord msgRcd = new MessageRecord();
                    msgRcd.setMessage(message);
                    msgRcd.setTime(Timestamp.valueOf(LocalDateTime.now()));
                    msgRcd.setSenderUid(uid);
                    msgRcd.setReceiverUid(receiver_uid);

                    COWebSocket socket = COWebSocket.webSocketMap.get(receiver_uid);
                    if (socket != null) {
                        JSONObject acceptMsgJson = new JSONObject();
                        acceptMsgJson.put("cmd", "acceptMessage");
                        acceptMsgJson.put("code", "0");
                        acceptMsgJson.put("msg", message);
                        acceptMsgJson.put("time", msgRcd.getTime().getTime());
                        acceptMsgJson.put("sender_uid", uid);
                        socket.sendMessage(acceptMsgJson.toJSONString());
                        msgRcd.setState("1");
                    } else {
                        msgRcd.setState("0");
                    }
                    suidRich.insert(msgRcd);
                    respJson.put("cmd", cmd);
                    respJson.put("code", "0");
                    respJson.put("msg", "成功");
                    sendMessage(respJson.toJSONString());
                    break;
                }
                default: {
                    JSONObject respJson = new JSONObject();
                    respJson.put("cmd", cmd);
                    respJson.put("code", "1");
                    respJson.put("msg", "未知指令");
                    sendMessage(respJson.toJSONString());
                    break;
                }
            }
        } else {
            JSONObject respJson = new JSONObject();
            respJson.put("cmd", cmd);
            respJson.put("code", "500");
            respJson.put("msg", "没有权限，请登录");
            sendMessage(respJson.toJSONString());
        }
    }

    public void sendMessage(String message) {
        sendThread.sendMessage(message);
    }

    private void appendLog(String msg) {
        JSONObject logData = new JSONObject();
        LocalDateTime time = LocalDateTime.now();
        logData.put("Time", time.format(DateTimeFormatter.ofPattern("E, yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)));
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

    private void print(String address, String msg) {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nCOWebSocket信息: " + (counter++) + ".（" + address + "） " + msg);
    }

    private class SendMessageThread extends Thread {
        private final Object lock = new Object();
        private final BlockingQueue<String> msgQue = new LinkedBlockingQueue<>();

        public SendMessageThread(String id) {
            super("Session(" + id + ")");
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    synchronized (lock) {
                        lock.wait();
                    }
                    while (msgQue.size() != 0) {
                        session.getBasicRemote().sendText(msgQue.take());
                    }
                }
            } catch (InterruptedException e) {
                interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            try {
                msgQue.put(message);
                synchronized (lock) {
                    lock.notify();
                }
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }
}
