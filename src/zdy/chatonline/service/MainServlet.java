package zdy.chatonline.service;

import com.alibaba.fastjson.JSONObject;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;
import zdy.chatonline.Constant;
import zdy.chatonline.log.Log;
import zdy.chatonline.service.request.RequestBody;
import zdy.chatonline.service.response.ResponseBody;
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
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = "/*", name = "Main")
public class MainServlet extends HttpServlet {
    private final static Log log = new Log();
    private int counter = 0;

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
                应包含参数：uid, pwd
                返回代码：0 成功；1 密码错误；2 uid参数不存在；3 pwd参数不存在;
             */
            case "/login": {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader("Content-type", "application/json");

                if (!reqBody.contains("uid")) {
                    respBody.add("{\"code\": \"2\",\"msg\": \"uid参数不存在\"}");
                    break;
                }
                if (!reqBody.contains("pwd")) {
                    respBody.add("{\"code\": \"3\",\"msg\": \"pwd参数不存在\"}");
                    break;
                }

                SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
                User qUser = new User(reqBody.getString("uid"));
                List<User> users = suidRich.select(qUser, "uid,password,nickname");
                for (User user : users) {
                    if (reqBody.get("pwd").equals(user.getPassword())) {
                        int maxAge = 10 * 60 * 60;//10小时
                        session.setMaxInactiveInterval(maxAge);
                        session.setAttribute("loggedIn", true);
                        session.setAttribute("uid", user.getUid());
                        Cookie cookieSId = new Cookie("JSESSIONID", session.getId());
                        cookieSId.setMaxAge(maxAge);
                        cookieSId.setPath("/");
                        resp.addCookie(cookieSId);
                        respBody.add("{\"code\": \"0\",\"msg\": \"" + user.getNickname() + "（" + user.getUid() +
                                "）登录成功\"}");
                        break;
                    }
                }
                if (!isLoggedIn(session)) {
                    respBody.add("{\"code\": \"1\",\"msg\": \"密码错误\"}");
                }
                break;
            }
            /*
                返回代码：0 成功；1 还未登录；
             */
            case "/logout": {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader("Content-type", "application/json");
                if (isLoggedIn(session)) {
                    session.setMaxInactiveInterval(1);
                    session.setAttribute("loggedIn", false);
                    Cookie cookieSId = new Cookie("JSESSIONID", session.getId());
                    cookieSId.setMaxAge(1);
                    cookieSId.setPath("/");
                    resp.addCookie(cookieSId);
                    respBody.add("{\"code\": \"0\",\"msg\": \"" + session.getAttribute("uid") + "登出成功\"}");
                } else {
                    respBody.add("{\"code\": \"1\",\"msg\": \"该用户未登录\"}");
                }
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
}
