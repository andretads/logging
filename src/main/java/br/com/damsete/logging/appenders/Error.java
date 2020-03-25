package br.com.damsete.logging.appenders;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static java.util.Optional.ofNullable;

public class Error {

    private String message;
    private String rootCause;
    private String className;
    private String methodName;
    private String rootClassName;
    private String rootMethodName;
    private String stacktrace;

    Error(Throwable exception) {
        this.message = ExceptionUtils.getMessage(exception);
        this.rootCause = ExceptionUtils.getRootCauseMessage(exception);

        StackTraceElement[] stackTraceElements = exception.getStackTrace();

        this.className = stackTraceElements[0].getClassName();
        this.methodName = stackTraceElements[0].getMethodName();

        Throwable rootCause = ExceptionUtils.getRootCause(exception);

        StackTraceElement[] rootCauseTraceElements = ofNullable(rootCause.getStackTrace()).orElse(new StackTraceElement[]{new StackTraceElement("", "", "", 0)});

        this.rootClassName = rootCauseTraceElements[0].getClassName();
        this.rootMethodName = rootCauseTraceElements[0].getMethodName();

        this.stacktrace = ExceptionUtils.getStackTrace(exception);
    }

    public String toLog() {
        return String.format("Error: message=%s, rootCause=%s, className=%s, methodName=%s, rootClassName=%s, rootMethodName=%s, stacktrace=%s",
                this.message, this.rootCause, this.className, this.methodName, this.rootClassName, this.rootMethodName, this.stacktrace);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
