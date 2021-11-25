package zdy.chatonline.service;

import zdy.chatonline.Constant;
import zdy.chatonline.service.response.ResponseBody;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@WebServlet(name = "Error")
public class ErrorServlet extends HttpServlet {
    private int counter = 0;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nErrorServlet信息: " + (counter++) + ".（" + req.getRemoteAddr() + ":"
                + req.getRemotePort() + "） GET " + req.getRequestURI() + " " + req.getProtocol());
        doing(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"
                , Locale.CHINA)) + "\r\nErrorServlet信息: " + (counter++) + ".（" + req.getRemoteAddr() + ":"
                + req.getRemotePort() + "） POST " + req.getRequestURI() + " " + req.getProtocol());
        doing(req, resp);
    }

    private void doing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseBody respBody = new ResponseBody(resp);

        Path filepath = Paths.get(Constant.configs.getString("DIRECTORY_PREFIX"), req.getRequestURI());
        InputStream fileInputStream = Files.newInputStream(filepath);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.addHeader("Content-type", "text/html;charset=utf-8");
        respBody.addFromStream(fileInputStream);
        respBody.send();
    }
}
