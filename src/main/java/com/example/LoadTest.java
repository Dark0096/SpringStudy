package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    private static AtomicInteger counter = new AtomicInteger(1);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {

        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8080/rest?idx={idx}";

        CyclicBarrier barrier = new CyclicBarrier(100);

        StopWatch main = new StopWatch();
        main.start();

        for (int j = 0; j < 100; j++) {
            es.submit(() -> {
                int idx = counter.addAndGet(1);

                barrier.await();

                log.info("Thread {}", idx);

                StopWatch sw = new StopWatch();
                sw.start();

                String response = restTemplate.getForObject(url, String.class, idx);

                sw.stop();
                log.info("Elapsed: {} {} / {}", idx, sw.getTotalTimeSeconds(), response);

                return null;
            });
        }

        es.shutdown();
        es.awaitTermination(200, TimeUnit.SECONDS);

        main.stop();
        log.info("Total: {}", main.getTotalTimeSeconds());
    }
}
