package br.com.damsete.logging.appenders;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Request {

    private final String system;
    private final String username;
    private final String url;
    private final String headers;
    private final String method;
    private final String payload;
    private final String handler;

    public Request(String system, String username, String url, String headers, String method,
                   String payload, String handler) {
        this.system = system;
        this.username = username;
        this.url = url;
        this.headers = headers;
        this.method = method;
        this.payload = payload;
        this.handler = handler;
    }

    public String toLog() {
        return String.format("Request: system=%s, username=%s, url=%s, headers=%s, method=%s, payload=%s, handler=%s",
                this.system, this.username, this.url, this.headers, this.method, this.payload, this.handler);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
