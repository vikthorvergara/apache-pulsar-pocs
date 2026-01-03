package com.vikthorvergara.pulsar.taskqueue.producer;

import com.vikthorvergara.pulsar.taskqueue.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
public class TaskProducer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TaskProducer.class);
    private static final String TASK_TOPIC = "persistent://public/default/task-queue";

    private final PulsarTemplate pulsarTemplate;

    public TaskProducer(PulsarTemplate pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(2000);

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 1; i <= 10; i++) {
            int taskNum = i;
            executor.submit(() -> produceTask(taskNum));
            Thread.sleep(300);
        }

        executor.shutdown();
    }

    private void produceTask(int taskNum) {
        String taskType = switch (taskNum % 3) {
            case 0 -> "EMAIL";
            case 1 -> "REPORT";
            default -> "BACKUP";
        };

        int priority = taskNum % 2 == 0 ? 10 : 5;

        var task = new Task(
            taskType,
            "Processing " + taskType + " task #" + taskNum,
            priority
        );

        try {
            pulsarTemplate.send(TASK_TOPIC, task);
            log.info("Produced task: type={}, priority={}, payload={}",
                task.getTaskType(), task.getPriority(), task.getPayload());
        } catch (Exception e) {
            log.error("Failed to produce task", e);
        }
    }
}
