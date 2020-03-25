package br.com.damsete.logging.filters;

import br.com.damsete.logging.appenders.Error;
import br.com.damsete.logging.appenders.Request;
import br.com.damsete.logging.appenders.Response;
import br.com.damsete.logging.utils.UniqueIDGenerator;
import br.com.damsete.logging.wrappers.RequestWrapper;
import br.com.damsete.logging.wrappers.ResponseWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger RESPONSE_LOGGER = LogManager.getLogger(Response.class);
    private static final Logger REQUEST_LOGGER = LogManager.getLogger(Request.class);
    private static final Logger ERROR_LOGGER = LogManager.getLogger(Error.class);

    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private UniqueIDGenerator generator;
    private String ignorePatterns;
    private String system;

    public LoggingFilter(RequestMappingHandlerMapping requestMappingHandlerMapping, UniqueIDGenerator generator,
                         String ignorePatterns, String system) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.generator = generator;
        this.ignorePatterns = ignorePatterns;
        this.system = system;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (StringUtils.isEmpty(this.system)) {
            throw new IllegalArgumentException("unidentified system");
        }

        if (this.ignorePatterns != null && request.getRequestURI().matches(this.ignorePatterns)) {
            chain.doFilter(request, response);
        } else {
            this.generator.generateAndSetMDC(request);

            String username = getUsername();
            String handler = getHandlerMethod(request);

            RequestWrapper wrappedRequest = new RequestWrapper(request);
            logRequest(wrappedRequest, username, this.system, handler);

            ResponseWrapper wrappedResponse = new ResponseWrapper(response);
            StopWatch watch = new StopWatch();
            try {
                watch.start();

                wrappedResponse.setHeader(UniqueIDGenerator.REQUEST_ID_HEADER_NAME, this.generator.getRequestID());
                wrappedResponse.setHeader(UniqueIDGenerator.CORRELATION_ID_HEADER_NAME, this.generator.getCorrelationID());

                chain.doFilter(wrappedRequest, wrappedResponse);
            } finally {
                logResponse(wrappedResponse, watch, username, this.system, handler,
                        (Throwable) request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE));
            }
        }
    }

    private void logRequest(RequestWrapper requestWrapper, String username, String system, String handler) throws IOException {
        String url = requestWrapper.getRequestURL().toString()
                + (requestWrapper.getQueryString() != null ? "?"
                + requestWrapper.getQueryString() : "");

        String payload = IOUtils.toString(requestWrapper.getInputStream(), requestWrapper.getCharacterEncoding());

        Request request = new Request(system.toLowerCase(), username, url, requestWrapper.getAllHeaders(),
                requestWrapper.getMethod(), payload, handler);

        REQUEST_LOGGER.info(request.toLog());
    }

    private void logResponse(ResponseWrapper responseWrapper, StopWatch watch, String username, String system,
                             String handler, Throwable throwable) throws IOException {
        watch.stop();

        String payload = IOUtils.toString(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());

        Response response = new Response(responseWrapper.getStatus(), watch.getTotalTimeMillis(), system.toLowerCase(),
                username, responseWrapper.getAllHeaders(), payload, handler, throwable);

        RESPONSE_LOGGER.info(response.toLog());

        response.getError().ifPresent(error -> ERROR_LOGGER.info(error.toLog()));
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "public";
        }
        return authentication.getName();
    }

    private String getHandlerMethod(HttpServletRequest request) {
        try {
            HandlerExecutionChain handler = this.requestMappingHandlerMapping.getHandler(request);
            if (Objects.nonNull(handler)) {
                HandlerMethod handlerMethod = (HandlerMethod) handler.getHandler();
                return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
