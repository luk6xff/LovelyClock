package com.igbt6.lovelyclock.configuration;

import com.igbt6.lovelyclock.presenter.RowHolder;
import com.google.common.base.Optional;

import org.immutables.value.Value;

/**
 * Created by Yuriy on 09.08.2017.
 */
@Value.Immutable
public abstract class EditedAlarm {
    @Value.Default
    public boolean isNew() {
        return false;
    }

    @Value.Default
    public int id() {
        return -1;
    }

    public abstract Optional<RowHolder> holder();

    @Value.Default
    public boolean isEdited() {
        return false;
    }
}
