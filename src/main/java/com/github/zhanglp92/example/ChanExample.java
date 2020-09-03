package com.github.zhanglp92.example;

import com.github.zhanglp92.chan.Chan;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class ChanExample {

    public static void f1() throws InterruptedException, ExecutionException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new ThreadPoolExecutor.AbortPolicy());

        Chan<Integer> ch = new Chan<>(40);
//        Chan<Integer> ch = new Chan<>();
        List<Future<?>> futureList = Lists.newArrayListWithCapacity(4);


        futureList.add(pool.submit(() -> {
            Random random = new Random(System.currentTimeMillis());
            for (int idx = 0; idx < 80; idx++) {
                ch.send(idx);
                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ch.close();
        }));

        Thread.sleep(1000);

        futureList.add(pool.submit(() -> ch.forEach(node -> System.out.printf("A -> %s\n", node))));
        futureList.add(pool.submit(() -> ch.forEach(node -> System.out.printf("B -> %s\n", node))));
        futureList.add(pool.submit(() -> ch.forEach(node -> System.out.printf("C -> %s\n", node))));

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

    public static void f2() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock(false);
        Condition c = lock.newCondition();

        lock.lockInterruptibly();
        c.signal();
        c.signal();
        c.signal();
        c.signal();
        lock.unlock();

        while (true) {
            log.info("condition await");
            lock.lockInterruptibly();
            c.await();
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        f1();
    }
}
