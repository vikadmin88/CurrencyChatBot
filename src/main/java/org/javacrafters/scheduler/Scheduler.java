
package org.javacrafters.scheduler;

import org.javacrafters.banking.CurrencyHolder;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private static final Map<Long, ScheduledFuture<?>> userSchedulers = new HashMap<>();
    private static ScheduledFuture<?> currencyScheduler;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private Scheduler() {
    }

    public static void addUserSchedule(Long userId, User user, int toHour) {

        int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int curMinutes = Calendar.getInstance().get(Calendar.MINUTE);
        int initDelay = 1;
        if (curHour > toHour) {
            initDelay = (24 - curHour + toHour) * 60 + curMinutes;
        } else if (curHour < toHour) {
            initDelay = (toHour - curHour) * 60 - curMinutes;
        } else {
            initDelay = (24 - curHour + toHour) * 60 - curMinutes;
        }

        final Runnable threadUserScheduledTask = () -> {
            if (user.isNotifyOn()) {
                LOGGER.info("Scheduler: Notified User: {}  {} Thread: {}", user.getId(), user.getName(), Thread.currentThread().getName());
                AppRegistry.getChatBot().userNotify(user);
            } else {
                LOGGER.info("Scheduler: Disabled for User: {} Thread: {}", user.getId(), Thread.currentThread().getName());
            }
        };
        if (AppRegistry.getConfIsProdMode()) {
            // production
            userSchedulers.put(userId, scheduler.scheduleAtFixedRate(threadUserScheduledTask, initDelay, 24*60, MINUTES));
        } else {
            // test !!! period, 1 MINUTES
            initDelay = 1;
            userSchedulers.put(userId, scheduler.scheduleAtFixedRate(threadUserScheduledTask, initDelay, 1, MINUTES));
        }
    }

    public static ScheduledFuture<?> getUserScheduler(Long userId) {
        return userSchedulers.get(userId);
    }
    public static void removeUserScheduler(Long userId) {
        getUserScheduler(userId).cancel(true);
        userSchedulers.remove(userId);
    }


    public static void addCurrencySchedule(int period) {

        int initDelay = 3;

        final Runnable threadCurrencyScheduledTask = () -> {
            LOGGER.info("CurrencyHolder got new rates. Next request in {} minutes. thread: {}", period,  Thread.currentThread().getName());
            CurrencyHolder.refreshRates();
        };
        currencyScheduler = scheduler.scheduleAtFixedRate(threadCurrencyScheduledTask, initDelay, period * 60L, SECONDS);
    }

    public static ScheduledFuture<?> getCurrencyScheduler() {
        return currencyScheduler;
    }
}