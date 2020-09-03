package com.github.zhanglp92.chan;

@FunctionalInterface
public interface SendWait<V> {

    V call();
}
