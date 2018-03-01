package com.igbt6.lovelyclock;

import android.content.ContentResolver;

import com.igbt6.lovelyclock.configuration.ImmutablePrefs;
import com.igbt6.lovelyclock.configuration.ImmutableStore;
import com.igbt6.lovelyclock.configuration.Store;
import com.igbt6.lovelyclock.interfaces.Alarm;
import com.igbt6.lovelyclock.interfaces.IAlarmsManager;
import com.igbt6.lovelyclock.interfaces.Intents;
import com.igbt6.lovelyclock.logger.Logger;
import com.igbt6.lovelyclock.logger.SysoutLogWriter;
import com.igbt6.lovelyclock.model.AlarmContainer;
import com.igbt6.lovelyclock.model.AlarmCore;
import com.igbt6.lovelyclock.model.AlarmCoreFactory;
import com.igbt6.lovelyclock.model.AlarmSetter;
import com.igbt6.lovelyclock.model.AlarmValue;
import com.igbt6.lovelyclock.model.Alarms;
import com.igbt6.lovelyclock.model.AlarmsScheduler;
import com.igbt6.lovelyclock.model.CalendarType;
import com.igbt6.lovelyclock.model.Calendars;
import com.igbt6.lovelyclock.model.ContainerFactory;
import com.igbt6.lovelyclock.model.ImmutableAlarmContainer;
import com.igbt6.lovelyclock.model.ImmutableDaysOfWeek;
import com.igbt6.lovelyclock.persistance.DatabaseQuery;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlarmsTest {
    private AlarmCore.IStateNotifier stateNotifierMock;
    private AlarmSetter alarmSetterMock;
    private TestScheduler testScheduler;
    private ImmutableStore store;
    private ImmutablePrefs prefs;
    private Logger logger;
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("---- " + description.getMethodName() + " ----");
        }
    };


    @Before
    public void setUp() {
        testScheduler = new TestScheduler();
        logger = Logger.create().addLogWriter(new SysoutLogWriter());

        prefs = ImmutablePrefs.builder()
                .preAlarmDuration(BehaviorSubject.createDefault(10))
                .snoozeDuration(BehaviorSubject.createDefault(10))
                .autoSilence(BehaviorSubject.createDefault(10))
                .is24HoutFormat(Single.just(true))
                .build();

        store = ImmutableStore.builder()
                .alarmsSubject(BehaviorSubject.<List<AlarmValue>>createDefault(new ArrayList<AlarmValue>()))
                .next(BehaviorSubject.createDefault(Optional.<Store.Next>absent()))
                .sets(PublishSubject.<Store.AlarmSet>create())
                .build();

        stateNotifierMock = mock(AlarmCore.IStateNotifier.class);
        alarmSetterMock = mock(AlarmSetter.class);

    }

    private Alarms createAlarms(DatabaseQuery query) {
        Calendars calendars = new Calendars() {
            @Override
            public Calendar now() {
                return Calendar.getInstance();
            }
        };
        AlarmsScheduler alarmsScheduler = new AlarmsScheduler(alarmSetterMock, logger, store, prefs, calendars);
        Alarms alarms = new Alarms(alarmsScheduler, query, new AlarmCoreFactory(logger,
                alarmsScheduler,
                stateNotifierMock,
                new TestHandlerFactory(testScheduler),
                prefs,
                store,
                calendars

        ), new TestContainerFactory(calendars));
        return alarms;
    }

    private Alarms createAlarms() {
        return createAlarms(mockQuery());
    }

    @android.support.annotation.NonNull
    private DatabaseQuery mockQuery() {
        final DatabaseQuery query = mock(DatabaseQuery.class);
        List<AlarmContainer> list = Lists.newArrayList();
        when(query.query()).thenReturn(Single.just(list));
        return query;
    }

    @Test
    public void create() {
        //when
        IAlarmsManager instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        newAlarm.enable(true);
        testScheduler.triggerActions();
        //verify
        store.alarms().test().assertValue(new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                return alarmValues.size() == 1 && alarmValues.get(0).isEnabled();
            }
        });
    }

    @Test
    public void deleteDisabledAlarm() {
        //when
        IAlarmsManager instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        testScheduler.triggerActions();
        instance.delete(newAlarm);
        testScheduler.triggerActions();
        //verify
        store.alarms().test().assertValue(new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                return alarmValues.size() == 0;
            }
        });
    }

    @Test
    public void deleteEnabledAlarm() {
        //when
        IAlarmsManager instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        testScheduler.triggerActions();
        newAlarm.enable(true);
        testScheduler.triggerActions();
        instance.getAlarm(0).delete();
        testScheduler.triggerActions();
        //verify
        store.alarms().test().assertValue(new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                return alarmValues.size() == 0;
            }
        });
    }

    @Test
    public void createThreeAlarms() {
        //when
        IAlarmsManager instance = createAlarms();
        instance.createNewAlarm();
        testScheduler.triggerActions();
        instance.createNewAlarm().enable(true);
        testScheduler.triggerActions();
        instance.createNewAlarm();
        testScheduler.triggerActions();
        //verify
        store.alarms().test().assertValueAt(0, new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                System.out.println(alarmValues);
                return alarmValues.size() == 3
                        && !alarmValues.get(0).isEnabled()
                        && alarmValues.get(1).isEnabled()
                        && !alarmValues.get(2).isEnabled();
            }
        });
    }

    static class DatabaseQueryMock extends DatabaseQuery {
        private ContainerFactory factory;

        public DatabaseQueryMock(ContainerFactory factory) {
            super(mock(ContentResolver.class), factory);
            this.factory = factory;
        }

        @Override
        public Single<List<AlarmContainer>> query() {
            AlarmContainer container =
                    ImmutableAlarmContainer.copyOf(factory.create())
                            .withId(100500)
                            .withIsEnabled(true)
                            .withLabel("hello");

            List<AlarmContainer> item = Lists.newArrayList(container);
            return Single.just(item);
        }
    }

    @Test
    public void alarmsFromMemoryMustBePresentInTheList() {
        //when
        Alarms instance = createAlarms(new DatabaseQueryMock(new TestContainerFactory(new Calendars() {
            @Override
            public Calendar now() {
                return Calendar.getInstance();
            }
        })));

        instance.start();

        //verify
        store.alarms().test()
                .assertValue(new Predicate<List<AlarmValue>>() {
                    @Override
                    public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                        System.out.println(alarmValues);
                        return alarmValues.size() == 1
                                && alarmValues.get(0).isEnabled()
                                && alarmValues.get(0).getLabel().equals("hello");
                    }
                });
    }

    @Test
    public void editAlarm() {
        //when
        IAlarmsManager instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        newAlarm.edit().withIsEnabled(true).withHour(7).commit();
        testScheduler.triggerActions();
        //verify
        store.alarms().test().assertValue(new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                return alarmValues.size() == 1
                        && alarmValues.get(0).isEnabled()
                        && alarmValues.get(0).getHour() == 7;
            }
        });
    }

    @Test
    public void firedAlarmShouldBeDisabledIfNoRepeatingIsSet() {
        //when
        Alarms instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        newAlarm.enable(true);
        testScheduler.triggerActions();

        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.NORMAL);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_ALERT_ACTION));

        newAlarm.dismiss();
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_DISMISS_ACTION));

        //verify
        store.alarms().test().assertValue(new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                return alarmValues.size() == 1
                        && !alarmValues.get(0).isEnabled();
            }
        });
    }

    @Test
    public void firedAlarmShouldBeRescheduledIfRepeatingIsSet() {
        //when
        Alarms instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        newAlarm.edit().withIsEnabled(true).withDaysOfWeek(ImmutableDaysOfWeek.of(1)).commit();
        testScheduler.triggerActions();

        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.NORMAL);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_ALERT_ACTION));

        newAlarm.dismiss();
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_DISMISS_ACTION));

        //verify
        store.alarms().test().assertValue(new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                return alarmValues.size() == 1
                        && alarmValues.get(0).isEnabled();
            }
        });
    }

    @Test
    public void changingAlarmWhileItIsFiredShouldReschedule() {
        //when
        Alarms instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        newAlarm.enable(true);
        testScheduler.triggerActions();

        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.NORMAL);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_ALERT_ACTION));

        newAlarm.edit().withDaysOfWeek(ImmutableDaysOfWeek.of(1)).withIsPrealarm(true).commit();
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_DISMISS_ACTION));

        //verify
        store.alarms().test().assertValue(new Predicate<List<AlarmValue>>() {
            @Override
            public boolean test(@NonNull List<AlarmValue> alarmValues) throws Exception {
                return alarmValues.size() == 1
                        && alarmValues.get(0).isEnabled();
            }
        });

        ArgumentCaptor<AlarmsScheduler.ScheduledAlarm> captor = ArgumentCaptor.forClass(AlarmsScheduler.ScheduledAlarm.class);
        verify(alarmSetterMock, atLeastOnce()).setUpRTCAlarm(captor.capture());

        assertEquals(newAlarm.getId(), captor.getValue().id);
    }


    @Test
    public void firedAlarmShouldBeStillEnabledAfterSnoozed() {
        //given
        Alarms instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        //TODO circle the time, otherwise the tests may fail around 0 hours
        newAlarm.edit().withIsEnabled(true).withHour(0).withDaysOfWeek(ImmutableDaysOfWeek.of(1)).withIsPrealarm(true).commit();
        testScheduler.triggerActions();
        //TODO verify

        //when pre-alarm fired
        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.PREALARM);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_PREALARM_ACTION));

        //when pre-alarm-snoozed
        newAlarm.snooze();
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_SNOOZE_ACTION));

        //when alarm fired
        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.NORMAL);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_ALERT_ACTION));

        //when alarm is snoozed
        newAlarm.snooze();
        testScheduler.triggerActions();
        verify(stateNotifierMock, times(2)).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_DISMISS_ACTION));

        newAlarm.delete();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ACTION_CANCEL_SNOOZE));
    }


    @Test
    public void snoozeToTime() {
        //given
        Alarms instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        //TODO circle the time, otherwise the tests may fail around 0 hours
        newAlarm.edit().withIsEnabled(true).withHour(0).withDaysOfWeek(ImmutableDaysOfWeek.of(1)).commit();
        testScheduler.triggerActions();
        //TODO verify

        //when alarm fired
        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.NORMAL);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_ALERT_ACTION));

        //when pre-alarm-snoozed
        newAlarm.snooze(23, 59);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_SNOOZE_ACTION));
    }

    @Test
    public void snoozePreAlarmToTime() {
        //given
        Alarms instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        //TODO circle the time, otherwise the tests may fail around 0 hours
        newAlarm.edit().withIsEnabled(true).withHour(0).withDaysOfWeek(ImmutableDaysOfWeek.of(1)).withIsPrealarm(true).commit();
        testScheduler.triggerActions();
        //TODO verify

        //when alarm fired
        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.PREALARM);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_PREALARM_ACTION));

        //when pre-alarm-snoozed
        newAlarm.snooze(23, 59);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_SNOOZE_ACTION));

        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.SNOOZE);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_ALERT_ACTION));
    }


    @Test
    public void prealarmTimedOutAndThenDisabled() {
        //given
        Alarms instance = createAlarms();
        Alarm newAlarm = instance.createNewAlarm();
        //TODO circle the time, otherwise the tests may fail around 0 hours
        newAlarm.edit().withIsEnabled(true).withHour(0).withDaysOfWeek(ImmutableDaysOfWeek.of(1)).withIsPrealarm(true).commit();
        testScheduler.triggerActions();
        //TODO verify

        //when alarm fired
        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.PREALARM);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_PREALARM_ACTION));

        instance.onAlarmFired((AlarmCore) newAlarm, CalendarType.NORMAL);
        testScheduler.triggerActions();
        verify(stateNotifierMock).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_ALERT_ACTION));

        //when pre-alarm-snoozed
        newAlarm.enable(false);
        testScheduler.triggerActions();
        verify(stateNotifierMock, atLeastOnce()).broadcastAlarmState(eq(newAlarm.getId()), eq(Intents.ALARM_DISMISS_ACTION));
    }
}