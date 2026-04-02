package com.iprody.xpaymentadapterapp.async;

public interface MessageHandler<T extends Message> {

    void handle(T message);
}
