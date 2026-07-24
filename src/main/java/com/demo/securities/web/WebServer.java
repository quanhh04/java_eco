package com.demo.securities.web;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {

    private final HttpServer httpServer;

    public WebServer(int port, Router router) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        this.httpServer.createContext("/", router);
    }

    public void start() {
        httpServer.start();
    }
}
