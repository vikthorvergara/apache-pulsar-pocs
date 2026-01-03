package com.vikthorvergara.pulsar.taskqueue.consumer;

import com.vikthorvergara.pulsar.taskqueue.model.Task;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.StructuredTaskScope;

@Component
public class TaskWorker {

    private static final Logger log = LoggerFactory.getLogger(TaskWorker.class);

    @PulsarListener(
        topics = "persistent://public/default/task-queue",
        subscriptionName = "task-worker-subscription",
        subscriptionType = SubscriptionType.Shared,
        concurrency = "3"
    )
    public void processTask(Task task) {
        log.info("Worker received task: type={}, priority={}, id={}",
            task.getTaskType(), task.getPriority(), task.getId());

        try (var scope = StructuredTaskScope.open()) {
            var validationTask = scope.fork(() -> validateTask(task));
            var executionTask = scope.fork(() -> executeTask(task));
            var auditTask = scope.fork(() -> auditTask(task));

            scope.join();

            validationTask.get();
            executionTask.get();
            auditTask.get();

            log.info("Task completed successfully: id={}, type={}",
                task.getId(), task.getTaskType());

        } catch (Exception e) {
            log.error("Task processing failed: id={}, type={}, error={}",
                task.getId(), task.getTaskType(), e.getMessage());
            task.setStatus("FAILED");
        }
    }

    private Boolean validateTask(Task task) {
        try {
            log.debug("Validating task: id={}", task.getId());
            Thread.sleep(50);

            if (task.getTaskType() == null || task.getPayload() == null) {
                throw new IllegalArgumentException("Invalid task data");
            }

            log.debug("Task validation passed: id={}", task.getId());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Validation interrupted", e);
        }
    }

    private String executeTask(Task task) {
        try {
            log.debug("Executing task: id={}, type={}", task.getId(), task.getTaskType());

            long processingTime = switch (task.getTaskType()) {
                case "EMAIL" -> 200;
                case "REPORT" -> 300;
                case "BACKUP" -> 400;
                default -> 100;
            };

            Thread.sleep(processingTime);

            task.setStatus("COMPLETED");
            log.debug("Task execution completed: id={}", task.getId());
            return "SUCCESS";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execution interrupted", e);
        }
    }

    private Void auditTask(Task task) {
        try {
            log.debug("Auditing task: id={}", task.getId());
            Thread.sleep(30);
            log.debug("Task audit recorded: id={}, type={}, priority={}",
                task.getId(), task.getTaskType(), task.getPriority());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Audit interrupted", e);
        }
    }
}
