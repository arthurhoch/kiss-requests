package io.github.arthurhoch.kissrequests.internal;

import java.io.*;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class MultipartBodyBuilder {

    private MultipartBodyBuilder() {}

    public static String generateBoundary() {
        return "----KissRequestsBoundary" + UUID.randomUUID().toString().replace("-", "");
    }

    public static HttpRequest.BodyPublisher buildBody(
            Map<String, String> fields,
            Map<String, Path> files,
            String boundary) throws IOException {

        List<InputStream> streams = new ArrayList<>();

        for (Map.Entry<String, String> entry : safeMap(fields).entrySet()) {
            streams.add(new ByteArrayInputStream(fieldPart(boundary, entry.getKey(), entry.getValue())));
        }

        for (Map.Entry<String, Path> entry : safeMap(files).entrySet()) {
            String name = entry.getKey();
            Path filePath = entry.getValue();
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new IOException("File not found or not readable: " + filePath);
            }
            String fileName = filePath.getFileName().toString();
            streams.add(new ByteArrayInputStream(filePreamble(boundary, name, fileName)));
            streams.add(Files.newInputStream(filePath));
            streams.add(new ByteArrayInputStream("\r\n".getBytes(StandardCharsets.UTF_8)));
        }

        streams.add(new ByteArrayInputStream(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8)));

        SequenceInputStream sequence = new SequenceInputStream(Collections.enumeration(streams));
        return HttpRequest.BodyPublishers.ofInputStream(() -> sequence);
    }

    public static long computeContentLength(Map<String, String> fields, Map<String, Path> files, String boundary) {
        long length = 0;
        for (Map.Entry<String, String> entry : safeMap(fields).entrySet()) {
            length += fieldPart(boundary, entry.getKey(), entry.getValue()).length;
        }
        for (Map.Entry<String, Path> entry : safeMap(files).entrySet()) {
            String name = entry.getKey();
            Path filePath = entry.getValue();
            String fileName = filePath.getFileName().toString();
            length += filePreamble(boundary, name, fileName).length;
            try {
                length += Files.size(filePath);
            } catch (IOException e) {
                return -1;
            }
            length += 2;
        }
        length += ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8).length;
        return length;
    }

    private static byte[] fieldPart(String boundary, String name, String value) {
        String part = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + escape(name) + "\"\r\n" +
                "\r\n" +
                value + "\r\n";
        return part.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] filePreamble(String boundary, String name, String fileName) {
        String part = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + escape(name) + "\"; filename=\"" + escape(fileName) + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "\r\n";
        return part.getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    private static <K, V> Map<K, V> safeMap(Map<K, V> map) {
        return map != null ? map : Map.of();
    }
}
