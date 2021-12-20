package zdy.chatonline.service;

import org.apache.catalina.servlets.DefaultServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@WebServlet(name = "Error")
public class ErrorServlet extends DefaultServlet {
    private long counter = 0;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        System.out.println(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA))
                        + "\r\nErrorServlet信息: " + (counter++) + ".（" + req.getRemoteAddr() + ":"
                        + req.getRemotePort() + "） GET " + req.getRequestURI() + " " + req.getProtocol()
        );
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        System.out.println(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA))
                        + "\r\nErrorServlet信息: " + (counter++) + ".（" + req.getRemoteAddr() + ":"
                        + req.getRemotePort() + "） POST " + req.getRequestURI() + " " + req.getProtocol()
        );
        super.doPost(req, resp);
    }

}
