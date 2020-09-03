package com.github.zhanglp92.chan;

public interface BlockingQueue<E> {

    void close() throws InterruptedException;

    boolean isDone();

    E take() throws InterruptedException;

    void offer(E e) throws InterruptedException;
}
