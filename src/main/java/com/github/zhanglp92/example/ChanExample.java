package com.github.zhanglp92.example;

import com.github.zhanglp92.chan.Chan;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Log4j2
public class ChanExample {

    public static void f1() throws InterruptedException, ExecutionException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new ThreadPoolExecutor.AbortPolicy());

        Chan<Integer> ch = new Chan<>(40);
        List<Future<?>> futureList = Lists.newArrayListWithCapacity(4);


        futureList.add(pool.submit(() -> {
            Random random = new Random(System.currentTimeMillis());

            for (int idx = 0; idx < 80; idx++) {
                try {
                    ch.send(idx);
                    Thread.sleep(random.nextInt(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ch.close();
        }));

        futureList.add(pool.submit(() -> {
            try {
                ch.forEach(node -> System.out.printf("A -> %s\n", node));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        futureList.add(pool.submit(() -> {
            try {
                ch.forEach(node -> System.out.printf("B -> %s\n", node));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        futureList.add(pool.submit(() -> {
            try {
                ch.forEach(node -> System.out.printf("C -> %s\n", node));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        futureList.forEach(future -> {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        log.info("end");
        pool.shutdown();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        f1();
    }
}
