package edu.uchicago.cs.db.common.functional;

@FunctionalInterface
public interface IntObjectConsumer<T> {
    void consume(int value, T value1);
}
