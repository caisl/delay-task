package com.caisl.dt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Scanner;

/**
 * DelayMessageApplication
 *
 * @author caisl
 * @since 2019-04-24
 */
@SpringBootApplication(scanBasePackages = {"com.caisl.dt"})
@EnableRetry
public class DelayTaskApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String port = scanner.nextLine();
        new SpringApplicationBuilder(DelayTaskApplication.class)
                .properties("server.port=" + port).run(args);
    }
}
