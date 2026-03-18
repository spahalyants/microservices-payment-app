package com.iprody.xpaymentadapterapp.async;

public interface AsyncListener<T extends Message> {

    void onMessage(T message);
}
