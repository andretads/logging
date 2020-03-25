package br.com.damsete.logging.appenders;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public class Response {

    private int status;
    private long duration;
    private String system;
    private String username;
    private String headers;
    private String payload;
    private String handler;
    private Error error;

    public Response(int status, long duration, String system, String username, String headers,
                    String payload, String handler, Throwable throwable) {
        this.status = status;
        this.duration = duration;
        this.system = system;
        this.username = username;
        this.headers = headers;
        this.payload = payload;
        this.handler = handler;
        this.error = ofNullable(throwable).map(Error::new).orElse(null);
    }

    public Optional<Error> getError() {
        return ofNullable(this.error);
    }

    public String toLog() {
        return String.format("Response(%s ms): status=%s, duration=%s, system=%s, username=%s, headers=%s, payload=%s, handler=%s, error=%s",
                this.duration, this.status, this.duration, this.system, this.username,
                this.headers, this.payload, this.handler, this.error != null);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
