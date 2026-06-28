package hr.algebra.gympr.camel.route;

import hr.algebra.gympr.camel.config.AppConfig;
import hr.algebra.gympr.camel.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import hr.algebra.gympr.camel.service.CamelAuthService;
import org.apache.camel.http.base.HttpOperationFailedException;

@Component
public class UpdateRoute extends RouteBuilder {

    private final ResponseProcessor responseProcessor;

    public UpdateRoute(ResponseProcessor responseProcessor) {
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
                .log("ERROR in update-" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("{\"error\": \"${exception.message}\", \"routeId\": \"update-%s\"}".formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=update-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));

        from("timer:updateTimer?period=60000")
                .routeId("update-" + AppConfig.ENTITY_NAME)
                .loadBalance().roundRobin()
                .to("direct:payload1-id1")
                .to("direct:payload2-id2")
                .to("direct:payload3-id3")
                .end()
                .log(">>> Triggered: update-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})")
                .setProperty(ResponseProcessor.OP_METHOD, constant("PUT"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, simple(AppConfig.BASE_URL + "/${header.targetId}"))
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${bean:camelAuthService.getToken}"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("activemq:queue:gympr-queue")
                .bean("payloadEncryptor", "encrypt")
                .to("file:%s?fileName=update-%s-id${header.targetId}-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: update-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})");

        from("direct:payload1-id1")
                .setBody(constant("""
                        {
                          "liftType": "BENCH_PRESS",
                          "muscleGroup": "CHEST",
                          "weightKg": 110.00,
                          "reps": 3,
                          "sets": 3,
                          "rpe": 8,
                          "bodyweightKg": 80.00,
                          "gymLocation": "Fit Fabrika Zagreb (Camel Test)",
                          "equipment": "Barbell, wrist wraps",
                          "liftDate": "2026-06-21",
                          "milestone": false,
                          "notes": "Updated Bench Press session [Camel Integration Test]"
                        }
                        """))
                .setHeader("targetId", constant(1));

        from("direct:payload2-id2")
                .setBody(constant("""
                        {
                          "liftType": "SQUAT",
                          "muscleGroup": "LEGS",
                          "weightKg": 160.00,
                          "reps": 1,
                          "sets": 1,
                          "rpe": 9,
                          "bodyweightKg": 81.00,
                          "gymLocation": "Fit Fabrika Zagreb (Camel Test)",
                          "equipment": "Squat rack, belt",
                          "liftDate": "2026-06-21",
                          "milestone": false,
                          "notes": "Updated Squat heavy single [Camel Integration Test]"
                        }
                        """))
                .setHeader("targetId", constant(2));

        from("direct:payload3-id3")
                .setBody(constant("""
                        {
                          "liftType": "DEADLIFT",
                          "muscleGroup": "BACK",
                          "weightKg": 200.00,
                          "reps": 1,
                          "sets": 1,
                          "rpe": 10,
                          "bodyweightKg": 80.50,
                          "gymLocation": "Fit Fabrika Zagreb (Camel Test)",
                          "equipment": "Rogue Ohio Bar, straps",
                          "liftDate": "2026-06-21",
                          "milestone": true,
                          "notes": "Updated Deadlift milestone PR! [Camel Integration Test]"
                        }
                        """))
                .setHeader("targetId", constant(3));
    }
}
