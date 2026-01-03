package com.vikthorvergara.pulsar.taskqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication(scanBasePackages = {
    "com.vikthorvergara.pulsar.taskqueue",
    "com.vikthorvergara.pulsar.common"
})
@EnablePulsar
public class TaskQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskQueueApplication.class, args);
    }
}
