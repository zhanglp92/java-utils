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

        // Chan<Integer> ch = new Chan<>();
        Chan<Integer> ch = new Chan<>(40);

        List<Future<?>> futureList = Lists.newArrayListWithCapacity(4);

        futureList.add(pool.submit(() -> {
            log.info("读取chan阻塞测试...");
            log.info("读取chan阻塞测试, data = {}", ch.recv());
        }));

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
            log.info("正在关闭chan");
            ch.close();
            log.info("chan已关闭");
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

        log.info("读取close chan不阻塞测试, data = {}", ch.recv());
        log.info("读取close chan不阻塞测试, data = {}", ch.recv());
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        f1();
    }
}
