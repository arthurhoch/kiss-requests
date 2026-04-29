# File Upload, Download, Stream, and Multipart

## File Upload

Upload a file from a `Path` to a remote server.

```java
Http http = Http.create();
HttpResult result = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf")).execute();
System.out.println(result.statusCode());
```

### Memory Safety

The file is streamed from disk using `BodyPublishers.ofFile()`. The library does not load the entire file into memory.

### Error Handling

```java
try {
    http.upload(HttpMethod.POST, "https://api.example.com/files",
            Map.of("Content-Type", "application/octet-stream"),
            Path.of("nonexistent.pdf")).execute();
} catch (HttpException e) {
    System.err.println("Upload failed: " + e.getMessage());
}
```

## Download to File

Download the response body directly to a file on disk.

```java
Http http = Http.create();
HttpDownloadResult result = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("downloaded.pdf")).execute();
System.out.println("Downloaded " + result.bytesWritten() + " bytes to " + result.file());
```

### Memory Safety

The response body is streamed directly to the file. The library never holds the full response in memory. Works for files of any size.

### Parent Directory Creation

If the target directory does not exist, it is created automatically before the download begins.

### Overwrite Behavior

If the target file exists, it is overwritten.

### Error Handling

```java
try {
    http.download(HttpMethod.GET, "https://api.example.com/files/999",
            Map.of(),
            Path.of("output.pdf")).execute();
} catch (HttpException e) {
    System.err.println("Download failed: " + e.getMessage());
    System.err.println("Status: " + e.statusCode());
}
```

## Stream Response as InputStream

Get the response body as an `InputStream` for caller-controlled reading.

```java
Http http = Http.create();
HttpStreamResult result = http.stream(HttpMethod.GET, "https://api.example.com/stream",
        Map.of(), null).execute();
try (InputStream is = result.inputStream()) {
    is.transferTo(System.out);
}
```

### Caller Responsibility

- **You must close the `InputStream`.** Use try-with-resources.
- The underlying HTTP connection may be held open until the stream is closed.
- The library does not buffer the entire response.

### Use Cases

- Large responses that should not be fully loaded into memory.
- Streaming APIs that deliver data incrementally.
- Processing response data as it arrives.

## Multipart/form-data

Send form fields and files as `multipart/form-data`.

```java
Http http = Http.create();
HttpResult result = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur", "email", "arthur@example.com"),
        Map.of("avatar", Path.of("photo.jpg"), "resume", Path.of("cv.pdf"))).execute();
System.out.println(result.statusCode());
```

### Content-Type Header

The `Content-Type: multipart/form-data; boundary=...` header is set automatically. Do not set it manually.

### Text Fields

Text fields are passed as `Map<String, String>`. Each entry becomes a form-data part:

```
--boundary
Content-Disposition: form-data; name="name"

Arthur
```

### File Fields

File fields are passed as `Map<String, Path>`. Each entry becomes a form-data part with the filename:

```
--boundary
Content-Disposition: form-data; name="avatar"; filename="photo.jpg"
Content-Type: application/octet-stream

<binary content>
```

### Memory Safety

File content is streamed from disk. The library does not load entire files into memory. Each file part is read sequentially during the upload.

### Empty Fields or Files

- Empty fields map: `Map.of()` — sends only file parts.
- Empty files map: `Map.of()` — sends only text fields.
- Both can be non-empty simultaneously.

### Error Handling

```java
try {
    http.multipart(HttpMethod.POST, "https://api.example.com/upload",
            Map.of(),
            Map.of("name", "Arthur"),
            Map.of("file", Path.of("missing.txt"))).execute();
} catch (HttpException e) {
    System.err.println("Upload failed: " + e.getMessage());
}
```

## toCurl for File Operations

```java
// Upload
http.upload(HttpMethod.POST, "https://api.example.com/files", Map.of(), Path.of("doc.pdf")).toCurl();
// curl -X POST 'https://api.example.com/files' --data-binary '@doc.pdf'

// Download
http.download(HttpMethod.GET, "https://api.example.com/files/123", Map.of(), Path.of("out.pdf")).toCurl();
// curl 'https://api.example.com/files/123' -o 'out.pdf'

// Multipart
http.multipart(HttpMethod.POST, "https://api.example.com/upload", Map.of(),
        Map.of("name", "Arthur"), Map.of("file", Path.of("photo.jpg"))).toCurl();
// curl -X POST 'https://api.example.com/upload' -F 'name=Arthur' -F 'file=@photo.jpg'
```

Use `HttpMethod` constants for standard methods. Raw method strings are accepted for custom or uncommon methods.
