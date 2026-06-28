package hr.algebra.gympr.camel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "hr.algebra.gympr.camel")
public class GymprCamelApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymprCamelApplication.class, args);
    }
}
