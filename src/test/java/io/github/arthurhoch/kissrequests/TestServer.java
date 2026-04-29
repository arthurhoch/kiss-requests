package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public final class TestServer {

    public static HttpServer create() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        server.createContext("/echo", exchange -> {
            byte[] body = readFully(exchange.getRequestBody());
            String response = exchange.getRequestMethod() + "\n"
                    + headersToString(exchange.getRequestHeaders()) + "\n"
                    + new String(body, StandardCharsets.UTF_8);
            sendText(exchange, 200, response);
        });

        server.createContext("/status/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            int code = Integer.parseInt(path.substring("/status/".length()));
            readFully(exchange.getRequestBody());
            sendText(exchange, code, "Status " + code);
        });

        server.createContext("/download", exchange -> {
            byte[] data = new byte[1024];
            new Random(42).nextBytes(data);
            exchange.sendResponseHeaders(200, data.length);
            exchange.getResponseBody().write(data);
            exchange.getResponseBody().close();
        });

        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sendText(exchange, 200, "OK");
        });

        server.createContext("/multipart", exchange -> {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            byte[] body = readFully(exchange.getRequestBody());
            String bodyStr = new String(body, StandardCharsets.UTF_8);

            String boundary = "";
            if (contentType != null && contentType.contains("boundary=")) {
                boundary = contentType.split("boundary=")[1];
            }

            int partCount = 0;
            if (!boundary.isEmpty()) {
                String marker = "--" + boundary;
                int idx = 0;
                while ((idx = bodyStr.indexOf(marker, idx)) != -1) {
                    partCount++;
                    idx += marker.length();
                }
                partCount = Math.max(0, partCount - 1);
            }

            String response = "boundary=" + boundary + " parts=" + partCount
                    + " bodyLen=" + body.length;
            sendText(exchange, 200, response);
        });

        server.createContext("/headers", exchange -> {
            readFully(exchange.getRequestBody());
            sendText(exchange, 200, headersToString(exchange.getRequestHeaders()));
        });

        server.createContext("/large-error", exchange -> {
            readFully(exchange.getRequestBody());
            char[] data = new char[8192];
            java.util.Arrays.fill(data, 'X');
            sendText(exchange, 500, new String(data));
        });

        return server;
    }

    private static void sendText(com.sun.net.httpserver.HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("X-Test-Status", String.valueOf(code));
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private static byte[] readFully(InputStream is) throws IOException {
        try (InputStream stream = is) {
            return stream.readAllBytes();
        }
    }

    private static String headersToString(Headers headers) {
        StringBuilder sb = new StringBuilder();
        for (var entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(String.join(", ", entry.getValue())).append("\n");
        }
        return sb.toString();
    }
}
