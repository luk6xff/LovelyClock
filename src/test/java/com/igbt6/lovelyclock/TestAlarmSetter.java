package com.igbt6.lovelyclock;

import com.igbt6.lovelyclock.model.AlarmSetter;
import com.igbt6.lovelyclock.model.AlarmsScheduler;

/**
 * Created by Yuriy on 25.06.2017.
 */
class TestAlarmSetter implements AlarmSetter {
    @Override
    public void removeRTCAlarm() {
        //NOP
    }

    @Override
    public void setUpRTCAlarm(AlarmsScheduler.ScheduledAlarm alarm) {
        //NOP
    }

    @Override
    public void fireNow(AlarmsScheduler.ScheduledAlarm firedInThePastAlarm) {
        //NOP
    }
}
