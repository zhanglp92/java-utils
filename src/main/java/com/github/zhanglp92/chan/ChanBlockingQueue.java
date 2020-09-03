package com.github.zhanglp92.chan;

import com.github.zhanglp92.util.Assert;
import lombok.extern.log4j.Log4j2;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 无缓冲队列
 */
@Log4j2
public class ChanBlockingQueue<E> implements BlockingQueue<E> {

    /**
     * 缓存数据
     */
    final Object[] items;

    /**
     * 写入/读取指针
     */
    private int takeIndex, putIndex, count;

    /**
     * 请求锁
     */
    final ReentrantLock lock;

    /**
     * buf是否已经关闭
     */
    private boolean isClose;

    /**
     * 等待发送的线程
     */
    private Queue<SendWait<E>> sendQueen = new LinkedBlockingQueue<>();

    /**
     * 等待接收的线程
     */
    private Queue<RecvWait<E>> recvQueen = new LinkedBlockingQueue<>();

    /**
     * 读写数据锁
     */
    final private Object readWriteLock = new Object();

    public ChanBlockingQueue() {
        this(0);
    }

    public ChanBlockingQueue(int cap) {
        this(cap, false);
    }

    public ChanBlockingQueue(int cap, boolean fair) {
        lock = new ReentrantLock(fair);
        items = new Object[cap];
    }

    public void close() throws InterruptedException {
        Lock lock = this.lock;
        lock.lockInterruptibly();

        try {
            isClose = true;
            while (!recvQueen.isEmpty()) {
                RecvWait<E> h = recvQueen.poll();
                if (h == null) {
                    continue;
                }

                log.info("close - recv , size = {}", recvQueen.size());

                E v = dequeue();
                if (v != null) {
                    // 1. 取缓存
                    h.call(v);
                } else {
                    SendWait<E> sh = sendQueen.poll();
                    if (sh != null) {
                        // 2. 取队列
                        h.call(sh.call());
                    } else {
                        // 3. 填空值
                        h.call(null);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isDone() {
        return count == 0 && sendQueen.size() == 0 && this.isClose;
    }

    public E take() throws InterruptedException {
        Lock lock = this.lock;
        lock.lockInterruptibly();
        try {
            // 1. 先取缓存
            E v = dequeue();
            if (v != null) {
                return v;
            }

            // 2. 如果待发送队列有数据, 则直接发送
            SendWait<E> h = sendQueen.poll();
            if (h != null) {
                return h.call();
            }

            // 3. 否则保存到待接收队列, 进行阻塞
            AtomicReference<E> av = new AtomicReference<>();
            Condition c = lock.newCondition();
            recvQueen.add((e) -> {
                c.signal();
                av.set(e);
            });
            c.await();
            return av.get();
        } finally {
            lock.unlock();
        }
    }

    public void offer(E e) throws InterruptedException {
        checkClose();

        Lock lock = this.lock;
        lock.lockInterruptibly();
        try {
            // 1. 写缓存
            if (enqueue(e)) {
                return;
            }

            // 2. 如果接收队列不为空, 则直接取队列线程
            RecvWait<E> h = recvQueen.poll();
            if (h != null) {
                h.call(e);
                return;
            }

            // 3. 否则保存到待发送队列, 进行阻塞
            Condition c = lock.newCondition();
            sendQueen.add(() -> {
                c.signal();
                return e;
            });
            c.await();
        } finally {
            lock.unlock();
        }
    }

    private boolean enqueue(E x) {
        if (items.length == 0) {
            return false;
        }

        synchronized (readWriteLock) {
            if (count == items.length) {
                return false;
            }

            final Object[] items = this.items;
            items[putIndex] = x;
            if (++putIndex == items.length) {
                putIndex = 0;
            }
            count++;
            return true;
        }
    }

    private E dequeue() {
        if (items.length == 0) {
            return null;
        }

        synchronized (readWriteLock) {
            if (count == 0) {
                return null;
            }

            final Object[] items = this.items;
            @SuppressWarnings("unchecked")
            E x = (E) items[takeIndex];
            items[takeIndex] = null;
            if (++takeIndex == items.length) {
                takeIndex = 0;
            }
            count--;
            return x;
        }
    }

    private void checkClose() {
        Assert.assertFalse("mustn't send to close chan", this.isClose);
    }
}
