package br.com.damsete.logging.configs;

import br.com.damsete.logging.filters.LoggingFilter;
import br.com.damsete.logging.utils.UniqueIDGenerator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ConfigurationProperties(prefix = "logging.filter")
public class LoggingConfig {

    @Getter
    @Setter
    private String ignorePatterns;
    @Getter
    @Setter
    private String system;
    @Getter
    @Setter
    private boolean enabled;

    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    public LoggingConfig(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Bean
    public UniqueIDGenerator generator() {
        return new UniqueIDGenerator();
    }

    @Bean
    @ConditionalOnProperty("logging.filter.enabled")
    public LoggingFilter loggingFilter() {
        return new LoggingFilter(this.requestMappingHandlerMapping, generator(), this.ignorePatterns, this.system);
    }
}
