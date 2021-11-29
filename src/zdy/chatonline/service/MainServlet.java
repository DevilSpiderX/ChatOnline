package zdy.chatonline.service;

import com.alibaba.fastjson.JSONObject;
import org.teasoft.bee.osql.Condition;
import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.ConditionImpl;
import zdy.chatonline.Constant;
import zdy.chatonline.log.Log;
import zdy.chatonline.service.request.RequestBody;
import zdy.chatonline.service.response.ResponseBody;
import zdy.chatonline.sql.Friends;
import zdy.chatonline.sql.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet(urlPatterns = "/*", name = "Main")
public class MainServlet extends HttpServlet {
    private final static Log log = new Log();
    private int counter = 0;
    private static final Map<String, HttpSession> UID_SESSION_TO_WS = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseBody respBody = new ResponseBody(resp);
        String path = req.getRequestURI();
        HttpSession session = req.getSession();

        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nMainServlet信息: " + (counter++) + ".（" + req.getRemoteAddr() + ":"
                + req.getRemotePort() + "） GET " + path + " " + req.getProtocol() + " " + req.getQueryString());

        if (path.equals("/")) {
            if (isLoggedIn(session)) {
                resp.sendRedirect("/index.html");
            } else {
                resp.sendRedirect("/login.html");
            }
        } else {
            Path filepath = Paths.get(Constant.configs.getString("WEB_DIRECTORY_PREFIX"), path);
            InputStream fileInputStream;
            if (filepath.toFile().canRead()) {
                fileInputStream = Files.newInputStream(filepath);
                if (path.endsWith(".html")) {//HTML网页
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.addHeader("Content-type", "text/html;charset=utf-8");
                    respBody.addFromStream(fileInputStream);
                } else if (path.endsWith(".css")) {//css文件
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.addHeader("Content-type", "text/css;charset=utf-8");
                    respBody.addFromStream(fileInputStream);
                } else if (path.endsWith(".js")) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.addHeader("Content-type", "application/x-javascript;charset=utf-8");
                    respBody.addFromStream(fileInputStream);
                } else if (path.endsWith(".ico")) {//图标文件
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.addHeader("Content-type", "image/x-icon");
                    respBody.addFromStream(fileInputStream);
                } else if (path.endsWith(".jpg")) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.addHeader("Content-type", "image/jpeg");
                    respBody.addFromStream(fileInputStream);
                } else if (path.endsWith(".png")) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.addHeader("Content-type", "image/png");
                    respBody.addFromStream(fileInputStream);
                } else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.addHeader("Content-type", "*/*");
                    resp.addHeader("Content-Encoding", "UTF-8");
                    respBody.addFromStream(fileInputStream);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                appendLog(req);
                return;
            }
        }
        respBody.send();
        appendLog(req);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestBody reqBody = new RequestBody(req);
        ResponseBody respBody = new ResponseBody(resp);
        String path = req.getRequestURI();
        HttpSession session = req.getSession();

        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nMainServlet信息: " + (counter++) + ".（" + req.getRemoteAddr() + ":"
                + req.getRemotePort() + "） POST " + path + " " + req.getProtocol() + " " + reqBody);

        switch (path) {
            /*
                登录

                应包含参数：uid, pwd
                返回代码：0 成功；1 密码错误；2 uid参数不存在；3 pwd参数不存在;4 uid不存在；
             */
            case "/login": {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader("Content-type", "application/json");

                JSONObject respJson = new JSONObject();
                if (!reqBody.contains("uid")) {
                    respJson.put("code", "2");
                    respJson.put("msg", "uid参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }
                if (!reqBody.contains("pwd")) {
                    respJson.put("code", "3");
                    respJson.put("msg", "pwd参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }

                SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
                String uid = reqBody.getString("uid");
                String pwd = reqBody.getString("pwd");
                User qUser = new User(uid);
                List<User> users = suidRich.select(qUser, "uid,password,nickname");
                if (users.size() == 0) {
                    respJson.put("code", "4");
                    respJson.put("msg", "uid不存在");
                } else {
                    User user = users.get(0);
                    if (pwd.equals(user.getPassword())) {
                        HttpSession oldSession;
                        if ((oldSession = getUidSession(uid)) != null) {
                            oldSession.setAttribute("loggedIn", false);
                            oldSession.setMaxInactiveInterval(5);
                        }
                        int maxAge = 10 * 60 * 60;//10小时
                        session.setMaxInactiveInterval(maxAge);
                        session.setAttribute("loggedIn", true);
                        session.setAttribute("uid", uid);
                        session.setAttribute("address", req.getRemoteAddr());
                        Cookie cookieSId = new Cookie("JSESSIONID", session.getId());
                        cookieSId.setMaxAge(maxAge);
                        cookieSId.setPath("/");
                        resp.addCookie(cookieSId);
                        UID_SESSION_TO_WS.put(uid, session);
                        respJson.put("code", "0");
                        respJson.put("msg", user.getNickname() + "（" + user.getUid() + "）登录成功");
                    } else {
                        respJson.put("code", "1");
                        respJson.put("msg", "密码错误");
                    }
                }
                respBody.add(respJson.toJSONString());
                break;
            }
            /*
                登出

                返回代码：0 成功；1 还未登录；
             */
            case "/logout": {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader("Content-type", "application/json");

                JSONObject respJson = new JSONObject();
                if (isLoggedIn(session)) {
                    session.setAttribute("loggedIn", false);
                    session.invalidate();

                    respJson.put("code", "0");
                    respJson.put("msg", session.getAttribute("uid") + "登出成功");
                } else {
                    respJson.put("code", "1");
                    respJson.put("msg", "该用户未登录");
                }
                respBody.add(respJson.toJSONString());
                break;
            }
            /*
                注册

                应包含参数：uid, pwd [,nick, age, gender, intro]
                返回代码：0 成功；1 失败；2 uid已存在；3 uid参数不存在；4 pwd参数不存在;
             */
            case "/register": {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader("Content-type", "application/json");

                JSONObject respJson = new JSONObject();
                if (!reqBody.contains("uid")) {
                    respJson.put("code", "3");
                    respJson.put("msg", "uid参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }
                if (!reqBody.contains("pwd")) {
                    respJson.put("code", "4");
                    respJson.put("msg", "pwd参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }

                SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
                String uid = reqBody.getString("uid");
                User qUser = new User(uid);
                if (suidRich.exist(qUser)) {
                    respJson.put("code", "2");
                    respJson.put("msg", "该用户名（" + uid + "）已存在");
                } else {
                    String password = reqBody.getString("pwd");
                    String nickName = uid, gender = "", introduction = "";
                    int age = 0;
                    if (reqBody.contains("nick")) {
                        nickName = reqBody.getString("nick");
                    }
                    if (reqBody.contains("age")) {
                        if (reqBody.get("age") instanceof Integer) {
                            age = (Integer) reqBody.get("age");
                        } else {
                            age = Integer.parseInt(reqBody.getString("age"));
                        }
                    }
                    if (reqBody.contains("gender")) {
                        gender = reqBody.getString("gender");
                    }
                    if (reqBody.contains("intro")) {
                        introduction = reqBody.getString("intro");
                    }
                    if (User.add_user(uid, password, nickName, age, gender, introduction) > 0) {
                        respJson.put("code", "0");
                        respJson.put("msg", "注册成功");
                    } else {
                        respJson.put("code", "1");
                        respJson.put("msg", "注册失败，数据库插入失败");
                    }
                }
                respBody.add(respJson.toJSONString());
                break;
            }
            /*
                添加好友

                应包含参数：own_uid, friend_uid
                返回代码：0 成功；1 失败；2 own_uid参数不存在；3 friend_uid参数不存在;4 没有权限；
             */
            case "/addFriend": {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader("Content-type", "application/json");

                JSONObject respJson = new JSONObject();
                if (!reqBody.contains("own_uid")) {
                    respJson.put("code", "2");
                    respJson.put("msg", "own_uid参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }
                if (!reqBody.contains("friend_uid")) {
                    respJson.put("code", "3");
                    respJson.put("msg", "friend_uid参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }
                if (!isLoggedIn(session)) {
                    respJson.put("code", "4");
                    respJson.put("msg", "没有权限，请登录");
                    respBody.add(respJson.toJSONString());
                    break;
                }

                SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
                String own_uid = reqBody.getString("own_uid");
                String friend_uid = reqBody.getString("friend_uid");

                Friends[] friends = new Friends[]{new Friends(), new Friends()};
                friends[0].setOwnUid(own_uid);
                friends[0].setFriendUid(friend_uid);
                friends[1].setOwnUid(friend_uid);
                friends[1].setFriendUid(own_uid);

                int count = suidRich.insert(friends);
                if (count == 2) {
                    respJson.put("code", "0");
                    respJson.put("msg", "添加成功");
                } else {
                    respJson.put("code", "1");
                    respJson.put("msg", "添加失败");
                }
                respBody.add(respJson.toJSONString());
                break;
            }
            /*
                删除好友

                应包含参数：own_uid, friend_uid
                返回代码：0 成功；1 失败；2 own_uid参数不存在；3 friend_uid参数不存在;4 没有权限；
             */
            case "/deleteFriend": {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader("Content-type", "application/json");

                JSONObject respJson = new JSONObject();
                if (!reqBody.contains("own_uid")) {
                    respJson.put("code", "2");
                    respJson.put("msg", "own_uid参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }
                if (!reqBody.contains("friend_uid")) {
                    respJson.put("code", "3");
                    respJson.put("msg", "friend_uid参数不存在");
                    respBody.add(respJson.toJSONString());
                    break;
                }
                if (!isLoggedIn(session)) {
                    respJson.put("code", "4");
                    respJson.put("msg", "没有权限，请登录");
                    respBody.add(respJson.toJSONString());
                    break;
                }

                SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
                String own_uid = reqBody.getString("own_uid");
                String friend_uid = reqBody.getString("friend_uid");
                Condition con = new ConditionImpl();
                con.op("own_uid", Op.equal, own_uid).and().op("friend_uid", Op.equal, friend_uid).or()
                        .op("own_uid", Op.equal, friend_uid).and().op("friend_uid", Op.equal, own_uid);
                List<Friends> friendsList = suidRich.select(new Friends(), con);

                if (friendsList.size() == 2) {
                    for (Friends friends : friendsList) {
                        suidRich.delete(friends);
                    }
                    respJson.put("code", "0");
                    respJson.put("msg", "删除成功");
                } else {
                    respJson.put("code", "1");
                    respJson.put("msg", "删除失败");
                }
                respBody.add(respJson.toJSONString());
                break;
            }
            default: {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                appendLog(req, reqBody.toString());
                return;
            }
        }

        respBody.send();
        appendLog(req, reqBody.toString());
    }

    private void appendLog(HttpServletRequest req, String body) {
        JSONObject logData = new JSONObject();
        LocalDateTime time = LocalDateTime.now();
        logData.put("Time", time.format(DateTimeFormatter.ofPattern("E, yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)));
        String address = req.getRemoteAddr() + ":" + req.getRemotePort();
        logData.put("Address", address);
        logData.put("Method", req.getMethod());
        logData.put("Path", req.getRequestURI());
        logData.put("HTTPVersion", req.getProtocol());
        StringBuilder headerText = new StringBuilder("{\r\n\t");
        Enumeration<String> hNames = req.getHeaderNames();
        while (hNames.hasMoreElements()) {
            String name = hNames.nextElement();
            headerText.append(name).append(": ")
                    .append(req.getHeader(name))
                    .append("\r\n\t");
        }
        headerText.append("\r\n}");
        logData.put("Headers", headerText.toString());
        if (body != null) {
            logData.put("Body", body);
        }
        synchronized (log) {
            log.writeLog(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)), logData);
        }
    }

    private void appendLog(HttpServletRequest req) {
        appendLog(req, null);
    }

    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn");
    }

    public static HttpSession getUidSession(String uid) {
        if (UID_SESSION_TO_WS.containsKey(uid)) {
            HttpSession session = UID_SESSION_TO_WS.get(uid);
            UID_SESSION_TO_WS.remove(uid);
            return session;
        }
        return null;
    }
}
