package hr.algebra.gympr.camel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    public static String BASE_URL;
    public static String OUTPUT_DIR;
    public static String TIMESTAMP_FORMAT;
    public static String ENTITY_NAME;

    public AppConfig(
        @Value("${gympr.api.base-url}") String baseUrl,
        @Value("${gympr.output.dir}") String outputDir,
        @Value("${gympr.timestamp.format}") String timestampFormat,
        @Value("${gympr.entity.name}") String entityName
    ) {
        BASE_URL = baseUrl;
        OUTPUT_DIR = outputDir;
        TIMESTAMP_FORMAT = timestampFormat;
        ENTITY_NAME = entityName;
    }
}
