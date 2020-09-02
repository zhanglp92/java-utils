package com.github.zhanglp92.chan;

import lombok.extern.log4j.Log4j2;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 实现简易的Go channel
 */
@Log4j2
public class Chan<V> {

    /**
     * 发送结束
     */
    private boolean isClose;

    /**
     * 数据缓冲区
     */
    private ArrayBlockingQueue<V> buf;

    final ReentrantLock lock;

    /**
     * 有缓冲chan
     *
     * @param cap 缓冲区长度, 0为无缓冲chan
     */
    public Chan(int cap) {
        this.buf = new ArrayBlockingQueue<>(cap);
        this.lock = new ReentrantLock();
    }

    /**
     * 关闭chan
     */
    public void close() {
        this.isClose = true;
        log.info("send finish");
    }

    /**
     * 发送数据
     */
    public void send(V v) throws Exception {
        if (this.isClose) {
            throw new Exception("mustn't send close chan");
        }
        this.buf.put(v);
    }

    /**
     * 接受数据
     */
    private V recv() throws InterruptedException {
        return this.buf.take();
    }

    /**
     * 迭代接受数据
     */
    public void forEach(Consumer<? super V> action) throws InterruptedException {
        Objects.requireNonNull(action);
        while (!this.buf.isEmpty() || !this.isClose) {
            action.accept(this.recv());
        }
        log.info("for finish");
    }
}
