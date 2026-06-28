package hr.algebra.gympr.camel.route;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import hr.algebra.gympr.camel.config.AppConfig;
import hr.algebra.gympr.camel.processor.ResponseProcessor;

import hr.algebra.gympr.camel.service.CamelAuthService;
import org.apache.camel.http.base.HttpOperationFailedException;

@Component
public class GetOneRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;

    public GetOneRoute(ResponseProcessor responseProcessor) {
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
                .log("ERROR in get-one-" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("{\"error\": \"${exception.message}\", \"routeId\": \"get-one-%s\"}".formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=get-one-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));

        from("timer:getOneTimer?period=15000")
                .routeId("get-one-" + AppConfig.ENTITY_NAME)
                .loadBalance().roundRobin()
                .to("direct:set-id-1")
                .to("direct:set-id-2")
                .to("direct:set-id-3")
                .to("direct:set-id-4")
                .to("direct:set-id-5")
                .end()
                .log(">>> Triggered: get-one-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})")
                .setProperty(ResponseProcessor.OP_METHOD, constant("GET"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, simple(AppConfig.BASE_URL + "/${header.targetId}"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Authorization", simple("Bearer ${bean:camelAuthService.getToken}"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("activemq:queue:gympr-queue")
                .bean("payloadEncryptor", "encrypt")
                .to("file:%s?fileName=get-one-%s-id${header.targetId}-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: get-one-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})");

        from("direct:set-id-1").setHeader("targetId", constant(1));
        from("direct:set-id-2").setHeader("targetId", constant(2));
        from("direct:set-id-3").setHeader("targetId", constant(3));
        from("direct:set-id-4").setHeader("targetId", constant(4));
        from("direct:set-id-5").setHeader("targetId", constant(5));
    }
}
