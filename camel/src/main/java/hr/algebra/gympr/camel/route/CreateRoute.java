package hr.algebra.gympr.camel.route;

import hr.algebra.gympr.camel.config.AppConfig;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import hr.algebra.gympr.camel.processor.ResponseProcessor;

import hr.algebra.gympr.camel.service.CamelAuthService;
import org.apache.camel.http.base.HttpOperationFailedException;

@Component
public class CreateRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;

    public CreateRoute(ResponseProcessor responseProcessor) {
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
                .log("ERROR in post" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("{\"error\": \"${exception.message}\", \"routeId\": \"post-one-%s\"}".formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=get-one-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));

        from("timer:createTimer?period=10000")
                .loadBalance().roundRobin()
                .to("direct:post1")
                .to("direct:post2")
                .to("direct:post3")
                .end()
                .routeId("create" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: create" + AppConfig.ENTITY_NAME)
                .setProperty(ResponseProcessor.OP_METHOD, constant("POST"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, constant(AppConfig.BASE_URL))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${bean:camelAuthService.getToken}"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("activemq:queue:gympr-queue")
                .bean("payloadEncryptor", "encrypt")
                .to("file:%s?fileName=create-one-%s-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: create" + AppConfig.ENTITY_NAME);


        from("direct:post1").setBody(constant("""
                {
                  "liftType": "BENCH_PRESS",
                  "muscleGroup": "CHEST",
                  "weightKg": 100.00,
                  "reps": 5,
                  "sets": 3,
                  "rpe": 8,
                  "bodyweightKg": 80.00,
                  "gymLocation": "Fit Fabrika Zagreb (Camel Test)",
                  "equipment": "Barbell, wrist wraps",
                  "liftDate": "2026-06-21",
                  "milestone": false,
                  "notes": "Bench press session 3 sets of 5 reps at 100kg [Camel Integration Test]"
                }
                """));
        from("direct:post2").setBody(constant("""
                {
                  "liftType": "SQUAT",
                  "muscleGroup": "LEGS",
                  "weightKg": 150.00,
                  "reps": 3,
                  "sets": 1,
                  "rpe": 9,
                  "bodyweightKg": 81.00,
                  "gymLocation": "Fit Fabrika Zagreb (Camel Test)",
                  "equipment": "Squat rack, belt",
                  "liftDate": "2026-06-21",
                  "milestone": true,
                  "notes": "New Squat PR single [Camel Integration Test]"
                }
                """));
        from("direct:post3").setBody(constant("""
                {
                  "liftType": "DEADLIFT",
                  "muscleGroup": "BACK",
                  "weightKg": 190.00,
                  "reps": 1,
                  "sets": 1,
                  "rpe": 10,
                  "bodyweightKg": 80.50,
                  "gymLocation": "Fit Fabrika Zagreb (Camel Test)",
                  "equipment": "Rogue Ohio Bar, straps",
                  "liftDate": "2026-06-21",
                  "milestone": false,
                  "notes": "Deadlift single rep heavy grinder [Camel Integration Test]"
                }
                """));
    }
}
