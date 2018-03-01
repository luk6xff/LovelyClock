package com.igbt6.lovelyclock.model;

import android.content.Context;
import android.content.Intent;

import com.igbt6.lovelyclock.interfaces.Intents;
import com.igbt6.lovelyclock.model.AlarmCore.IStateNotifier;

/**
 * Broadcasts alarm state with an intent
 * 
 * @author Yuriy
 * 
 */
public class AlarmStateNotifier implements IStateNotifier {

    private final Context mContext;

    public AlarmStateNotifier(Context context) {
        mContext = context;
    }

    @Override
    public void broadcastAlarmState(int id, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(Intents.EXTRA_ID, id);
        mContext.sendBroadcast(intent);
    }
}
