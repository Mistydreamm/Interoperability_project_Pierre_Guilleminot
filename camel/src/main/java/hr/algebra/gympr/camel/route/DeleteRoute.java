package hr.algebra.gympr.camel.route;

import hr.algebra.gympr.camel.config.AppConfig;
import hr.algebra.gympr.camel.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import hr.algebra.gympr.camel.service.CamelAuthService;
import org.apache.camel.http.base.HttpOperationFailedException;

@Component
public class DeleteRoute extends RouteBuilder {

    private final ResponseProcessor responseProcessor;

    public DeleteRoute(ResponseProcessor responseProcessor) {
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
                .log("ERROR in delete-" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("{\"error\": \"${exception.message}\", \"routeId\": \"delete-%s\"}".formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=delete-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));

        from("timer:deleteTimer?period=60000")
                .routeId("delete-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: delete-" + AppConfig.ENTITY_NAME)
                .setProperty(ResponseProcessor.OP_METHOD, constant("POST"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, constant(AppConfig.BASE_URL))
                .setBody(constant("""
                        {
                          "liftType": "PULL_UP",
                          "muscleGroup": "BACK",
                          "weightKg": 20.00,
                          "reps": 5,
                          "sets": 3,
                          "rpe": 7,
                          "bodyweightKg": 80.00,
                          "gymLocation": "Temp Gym (Camel Test)",
                          "equipment": "Pull-up bar, belt",
                          "liftDate": "2026-06-21",
                          "milestone": false,
                          "notes": "Temporary record to be deleted by Camel [Camel Integration Test]"
                        }
                        """))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${bean:camelAuthService.getToken}"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .unmarshal().json()
                .setHeader("targetId", simple("${body[id]}"))
                .log(">>> Dynamic ID created for deletion: ${header.targetId}")
                .setHeader(Exchange.HTTP_METHOD, constant("DELETE"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))
                .setProperty(ResponseProcessor.OP_METHOD, constant("DELETE"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, simple(AppConfig.BASE_URL + "/${header.targetId}"))
                .setBody(simple(null))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("activemq:queue:gympr-queue")
                .bean("payloadEncryptor", "encrypt")
                .to("file:%s?fileName=delete-%s-id${header.targetId}-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: delete-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})");
    }
}
