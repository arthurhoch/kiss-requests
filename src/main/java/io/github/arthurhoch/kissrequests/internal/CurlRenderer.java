package io.github.arthurhoch.kissrequests.internal;

import io.github.arthurhoch.kissrequests.HttpCall;

import java.nio.file.Path;
import java.util.Map;

public final class CurlRenderer {

    private CurlRenderer() {}

    public static String render(HttpCall<?> call) {
        StringBuilder sb = new StringBuilder("curl");

        String method = call.method();
        if (!"GET".equals(method)) {
            sb.append(" -X ").append(method);
        }

        sb.append(' ').append(shellEscape(call.url()));

        for (Map.Entry<String, String> header : call.headers().entrySet()) {
            if (call.callType() == CallType.MULTIPART && "Content-Type".equalsIgnoreCase(header.getKey())) {
                continue;
            }
            sb.append(" -H ").append(shellEscape(header.getKey() + ": " + header.getValue()));
        }

        switch (call.callType()) {
            case TEXT -> {
                if (call.body() != null) {
                    sb.append(" --data-raw ").append(shellEscape(call.body()));
                }
            }
            case UPLOAD -> {
                Path file = call.file();
                if (file != null) {
                    sb.append(" --data-binary ").append(shellEscape("@" + file));
                }
            }
            case DOWNLOAD -> {
                Path target = call.targetPath();
                if (target != null) {
                    sb.append(" -o ").append(shellEscape(target.toString()));
                }
            }
            case STREAM -> {
                if (call.body() != null) {
                    sb.append(" --data-raw ").append(shellEscape(call.body()));
                }
            }
            case MULTIPART -> {
                if (call.fields() != null) {
                    for (Map.Entry<String, String> field : call.fields().entrySet()) {
                        sb.append(" -F ").append(shellEscape(field.getKey() + "=" + field.getValue()));
                    }
                }
                if (call.fileFields() != null) {
                    for (Map.Entry<String, Path> fileField : call.fileFields().entrySet()) {
                        sb.append(" -F ").append(shellEscape(fileField.getKey() + "=@" + fileField.getValue()));
                    }
                }
            }
        }

        return sb.toString();
    }

    static String shellEscape(String value) {
        if (value == null) return "''";
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
