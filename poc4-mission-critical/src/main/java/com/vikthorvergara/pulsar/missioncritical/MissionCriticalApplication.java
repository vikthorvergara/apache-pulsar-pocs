package com.vikthorvergara.pulsar.missioncritical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication(scanBasePackages = {
    "com.vikthorvergara.pulsar.missioncritical",
    "com.vikthorvergara.pulsar.common"
})
@EnablePulsar
public class MissionCriticalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MissionCriticalApplication.class, args);
    }
}
