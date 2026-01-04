package com.vikthorvergara.pulsar.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication(scanBasePackages = {
    "com.vikthorvergara.pulsar.rpc",
    "com.vikthorvergara.pulsar.common"
})
@EnablePulsar
public class RpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcApplication.class, args);
    }
}
