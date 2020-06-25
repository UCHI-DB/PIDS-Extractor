package edu.uchicago.cs.db.common.functional;

@FunctionalInterface
public interface IntIntConsumer {

    public void consume(int index, int value);
}
