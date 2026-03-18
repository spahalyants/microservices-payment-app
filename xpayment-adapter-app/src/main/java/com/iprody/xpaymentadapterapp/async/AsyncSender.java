package com.iprody.xpaymentadapterapp.async;

public interface AsyncSender<T extends Message> {

    void send(T message);
}

