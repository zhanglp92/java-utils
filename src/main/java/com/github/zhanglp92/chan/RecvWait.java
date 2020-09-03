package com.github.zhanglp92.chan;

@FunctionalInterface
public interface RecvWait<V> {

    void call(V v);
}
