
package org.javacrafters.scheduler;

import org.javacrafters.banking.CurrencyHolder;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private static int userPeriodRun = 24*60;
    private static final DateFormat DATEFORMAT = new SimpleDateFormat("HH");

    static {
        DATEFORMAT.setTimeZone(TimeZone.getTimeZone(AppRegistry.getConfTimeZone()));
    }
    private Scheduler() {
    }

    public static void addUserSchedule(Long userId, User user, int toHour) {

        if (user == null) {
            return;
        }
        int curHour = Integer.parseInt(DATEFORMAT.format(new Date()));
        int curMinutes = Calendar.getInstance().get(Calendar.MINUTE);
        int initDelay = 1;

        if (curHour > toHour) {
            initDelay = (24 - curHour + toHour) * 60 + curMinutes;
        } else if (curHour < toHour) {
            initDelay = (toHour - curHour) * 60 - curMinutes;
        } else {
            initDelay = (24 - curHour + toHour) * 60 - curMinutes;
        }

        Runnable threadUserScheduledTask = () -> {
            if (user.isNotifyOn()) {
                LOGGER.info("Notified User: {}  {} Thread: {}", user.getId(), user.getName(), Thread.currentThread().getName());
                AppRegistry.getChatBot().userNotify(user);
            } else {
                LOGGER.info("Disabled for User: {} Thread: {}", user.getId(), Thread.currentThread().getName());
            }
        };

        if (AppRegistry.getConfIsDevMode()) {
            initDelay = 1;
            userPeriodRun = 1;
        }
        userSchedulers.put(userId, scheduler.scheduleAtFixedRate(threadUserScheduledTask, initDelay, userPeriodRun, MINUTES));
        LOGGER.info("User: {} set to {}:00 Run in {} minutes. Task active: {}", user.getId(), user.getNotifyTime(), initDelay, !getUserScheduler(userId).isCancelled());
    }

    public static ScheduledFuture<?> getUserScheduler(Long userId) {
        return userSchedulers.get(userId);
    }
    public static void removeUserScheduler(Long userId) {
        if (getUserScheduler(userId) != null) {
            getUserScheduler(userId).cancel(true);
            userSchedulers.remove(userId);
        }
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