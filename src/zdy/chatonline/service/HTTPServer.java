package zdy.chatonline.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import zdy.chatonline.Constant;

import java.io.File;

public class HTTPServer {
    private final Tomcat tomcat;
    private final int port;

    public HTTPServer(int port) {
        this.port = port;
        tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.setConnector(connector);

        Context ctx = tomcat.addWebapp("", new File(Constant.WEB_DIRECTORY).
                getAbsolutePath());
        Tomcat.addServlet(ctx, "Main", new MainServlet());
        Tomcat.addServlet(ctx, "Error", new ErrorServlet());

        ctx.addServletMappingDecoded("/*", "Main");

        setErrorPage(ctx);
    }

    public void startServer() {
        try {
            tomcat.start();
            System.out.println("服务器启动成功");
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        System.out.println("正在关闭服务器......");
        try {
            tomcat.stop();
            System.out.println("服务器关闭成功");
        } catch (LifecycleException e) {
            e.printStackTrace();
        }

    }

    public Tomcat getTomcat() {
        return tomcat;
    }

    public int getPort() {
        return port;
    }

    public void setErrorPage(Context ctx) {
        JSONArray pageConfigs = Constant.ERROR_PAGE;
        for (int i = 0; i < pageConfigs.size(); i++) {
            JSONObject pageConfig = pageConfigs.getJSONObject(i);
            ErrorPage page = new ErrorPage();
            page.setErrorCode(pageConfig.getInteger("CODE"));
            page.setLocation(pageConfig.getString("LOCATION"));
            ctx.addErrorPage(page);
            ctx.addServletMappingDecoded(page.getLocation(), "Error");
        }
    }
}
