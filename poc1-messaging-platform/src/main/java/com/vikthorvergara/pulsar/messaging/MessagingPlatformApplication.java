package com.vikthorvergara.pulsar.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication(scanBasePackages = {
    "com.vikthorvergara.pulsar.messaging",
    "com.vikthorvergara.pulsar.common"
})
@EnablePulsar
public class MessagingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingPlatformApplication.java, args);
    }
}
