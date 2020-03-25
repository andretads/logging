package br.com.damsete.logging.wrappers;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseWrapper extends HttpServletResponseWrapper {

    private ServletOutputStream outputStream;
    private ServletOutputStreamWrapper copier;
    private PrintWriter writer;

    public ResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }

        if (this.outputStream == null) {
            this.outputStream = getResponse().getOutputStream();
            this.copier = new ServletOutputStreamWrapper(this.outputStream);
        }

        return this.copier;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }

        if (this.writer == null) {
            this.copier = new ServletOutputStreamWrapper(getResponse().getOutputStream());
            this.writer = new PrintWriter(new OutputStreamWriter(this.copier, getResponse().getCharacterEncoding()), true);
        }

        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (this.writer != null) {
            this.writer.flush();
        } else if (this.outputStream != null) {
            this.copier.flush();
        }
    }

    public byte[] getContentAsByteArray() {
        if (this.copier != null) {
            return this.copier.getCopy();
        } else {
            return new byte[0];
        }
    }

    public String getAllHeaders() {
        final Map<String, String> headers = new HashMap<>();
        getHeaderNames().forEach(it -> headers.put(it, getHeader(it)));
        return headers.keySet().stream().map(key -> key + "=" + headers.get(key)).collect(Collectors.joining(", ", "{", "}"));
    }
}
