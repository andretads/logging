package br.com.damsete.logging.utils;

import org.apache.logging.log4j.ThreadContext;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class UniqueIDGenerator {

    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    public void generateAndSetMDC(HttpServletRequest request) {
        ThreadContext.remove(CORRELATION_ID_HEADER_NAME);
        ThreadContext.remove(REQUEST_ID_HEADER_NAME);

        String requestId = UUID.randomUUID().toString();
        ThreadContext.put(REQUEST_ID_HEADER_NAME, requestId);

        String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        ThreadContext.put(CORRELATION_ID_HEADER_NAME, correlationId);
    }

    public String getCorrelationID() {
        return ThreadContext.get(CORRELATION_ID_HEADER_NAME);
    }

    public String getRequestID() {
        return ThreadContext.get(REQUEST_ID_HEADER_NAME);
    }
}
