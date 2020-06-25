package edu.uchicago.cs.db.common.functional;

@FunctionalInterface
public interface IntDoubleConsumer {

    void consume(int index, double value);
}
