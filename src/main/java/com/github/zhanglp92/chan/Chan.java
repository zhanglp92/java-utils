package com.github.zhanglp92.chan;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 实现简易的Go channel, 不保存null数据
 */
@Log4j2
public class Chan<V> {

    /**
     * 数据缓冲区
     */
    private BlockingQueue<V> buf;

    /**
     * 无缓冲
     */
    public Chan() {
        this(0);
    }

    /**
     * 有缓冲chan
     *
     * @param cap 缓冲区长度, 0为无缓冲chan
     */
    public Chan(int cap) {
        this.buf = new ChanBlockingQueue<>(cap);
    }

    /**
     * 关闭chan
     */
    @SneakyThrows
    public void close() {
        this.buf.close();
    }

    /**
     * 发送数据
     */
    @SneakyThrows
    public void send(V v) {
        this.buf.offer(v);
    }

    /**
     * 批量发送数据. 数组
     */
    @SafeVarargs
    @SneakyThrows
    public final void send(V... elements) {
        for (V v : elements) {
            send(v);
        }
    }

    /**
     * 批量发送数据. 迭代
     */
    public final void send(Iterable<? extends V> elements) {
        elements.forEach(this::send);
    }

    /**
     * 接受数据(需要保证有数据, 不然会阻塞出不来)
     */
    @SneakyThrows
    public V recv() {
        return this.buf.take();
    }

    /**
     * 迭代接受数据
     */
    @SneakyThrows
    public void forEach(Consumer<? super V> action) {
        Objects.requireNonNull(action);
        while (!this.buf.isDone()) {
            V v = this.recv();
            if (v != null) {
                action.accept(v);
            }
        }
    }
}
