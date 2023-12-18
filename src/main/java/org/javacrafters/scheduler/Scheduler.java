
package org.javacrafters.scheduler;

import org.javacrafters.banking.CurrencyHolder;
import org.javacrafters.core.ChatBot;
import org.javacrafters.user.User;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;


public class Scheduler {

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

        public ScheduledFuture<?> userSchedule(ChatBot bot, User user, int toHour) {

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
            // for test
            initDelay = 1;

            final Runnable threadUserScheduledTask = () -> {
                System.out.println(".");
                if (user.isNotifyOn()) {
                    System.out.println("Notified user: " + user.getId() +" "+ user.getName());
                    // while testing don't
                    bot.userNotify(user);
                }
            };

            // production
//            return scheduler.scheduleAtFixedRate(threadUserScheduledTask, initDelay, 24*60, MINUTES);
            // test !!! period, 1 MINUTES
            return scheduler.scheduleAtFixedRate(threadUserScheduledTask, initDelay, 1, MINUTES);
            // test !!! period, SECONDS
//            return scheduler.scheduleAtFixedRate(threadUserScheduledTask, initDelay, 3, SECONDS);
        }

        public void currencySchedule(int period) {

            int initDelay = 0;

            final Runnable threadCurrencyScheduledTask = () -> {
                System.out.println("Get new currency rate...");
                CurrencyHolder.refreshRates();
            };

            // production
            scheduler.scheduleAtFixedRate(threadCurrencyScheduledTask, initDelay, 1, MINUTES);
        }
}
