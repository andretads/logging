package br.com.damsete.logging.wrappers;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            body = IOUtils.toByteArray(request.getInputStream());
        } catch (IOException ex) {
            body = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ServletInputStream() {
            public boolean isFinished() {
                return false;
            }

            public boolean isReady() {
                return true;
            }

            public void setReadListener(ReadListener readListener) {
            }

            ByteArrayInputStream byteArray = new ByteArrayInputStream(body);

            @Override
            public int read() {
                return byteArray.read();
            }
        };
    }

    public String getAllHeaders() {
        final Map<String, String> headers = new HashMap<>();
        getHeaderNames().asIterator().forEachRemaining(it -> headers.put(it, getHeader(it)));
        return headers.keySet().stream().map(key -> key + "=" + headers.get(key)).collect(Collectors.joining(", ", "{", "}"));
    }
}
