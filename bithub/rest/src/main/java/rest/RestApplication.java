package rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starter REST-serveren for BIThub.
 */
@SpringBootApplication
public class RestApplication {

    /**
     * Starter Spring Boot med innebygd webserver.
     *
     * @param args argumenter fra kommandolinjen
     */
    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }
}