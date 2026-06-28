package hr.algebra.gympr.camel.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import hr.algebra.gympr.camel.config.AppConfig;
import hr.algebra.gympr.camel.processor.ResponseProcessor;

import hr.algebra.gympr.camel.service.CamelAuthService;
import org.apache.camel.http.base.HttpOperationFailedException;

@Component
public class GetAllRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;

    public GetAllRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {
        onException(HttpOperationFailedException.class)
                .onWhen(exchange -> {
                    HttpOperationFailedException e = exchange.getException(HttpOperationFailedException.class);
                    return e != null && e.getStatusCode() == 401;
                })
                .maximumRedeliveries(1)
                .redeliveryDelay(0)
                .process(exchange -> {
                    System.out.println("Camel token expired (401). Attempting token refresh...");
                    CamelAuthService authService = exchange.getContext().getRegistry().lookupByNameAndType("camelAuthService", CamelAuthService.class);
                    authService.tryTokenRefresh();
                })
                .setHeader("Authorization", simple("Bearer ${bean:camelAuthService.getToken}"));

        onException(Exception.class)
                .handled(true)
                .log("ERROR in get-all-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setBody(simple("{\"error\": \"${exception.message}\", \"routeId\": \"get-all-%s\"}".formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=get-all-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));

        from("timer:getAllTimer?period=10000")
                .routeId("get-all-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: get-all-" + AppConfig.ENTITY_NAME)
                .setProperty(ResponseProcessor.OP_METHOD, constant("GET"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, constant(AppConfig.BASE_URL))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Authorization", simple("Bearer ${bean:camelAuthService.getToken}"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("activemq:queue:gympr-queue")
                .bean("payloadEncryptor", "encrypt")
                .to("file:%s?fileName=get-all-%s-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: get-all-" + AppConfig.ENTITY_NAME);
    }
}
