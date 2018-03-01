package com.igbt6.lovelyclock.interfaces;

import com.igbt6.lovelyclock.model.AlarmChangeData;

import org.immutables.value.Value;

import io.reactivex.functions.Consumer;

@Value.Immutable
public abstract class AlarmEditor implements AlarmChangeData {
    public abstract Consumer<AlarmChangeData> callback();

    public void commit() {
        try {
            callback().accept(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
