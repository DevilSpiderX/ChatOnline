package zdy.chatonline;

import zdy.chatonline.service.HTTPServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("初始化中......");
        int port = Constant.PORT;
        HTTPServer httpServer = new HTTPServer(port);
        new Thread(httpServer::startServer, "myServer").start();
    }
}
