package com.igbt6.lovelyclock.model;

import android.database.Cursor;

/**
 * Created by Yuriy on 24.06.2017.
 */

public interface ContainerFactory {
    AlarmContainer create();

    AlarmContainer create(Cursor cursor);
}
