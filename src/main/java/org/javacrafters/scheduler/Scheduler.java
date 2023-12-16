
package org.javacrafters.scheduler;

import org.javacrafters.core.ChatBot;
import org.javacrafters.user.User;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;


public class Scheduler {

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
//    protected final ExecutorService service = Executors.newFixedThreadPool(2);

        public ScheduledFuture<?> schedule(ChatBot bot, User user, int toHour) {

            int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int curMinutes = Calendar.getInstance().get(Calendar.MINUTE);
//            int initDelay = 1;
            int initDelay = curHour > toHour ? ((24 - curHour + toHour) * 60 + curMinutes) : ((toHour - curHour) * 60 - curMinutes);

            final Runnable threadTask = new Runnable() {

                public void run() {
                    System.out.println(".");
                    if (user.isNotifyOn()) {
                        System.out.println("Notified user: " + user.getId() +" "+ user.getName());
                        // while testing don't
                        bot.userNotify(user);
                    }
                }
            };

//            ScheduledFuture<?> notifyTask = scheduler.scheduleAtFixedRate(threadTask, initDelay, 24*60, MINUTES);
            // test !!! period, 1 MINUTES
            ScheduledFuture<?> notifyTask = scheduler.scheduleAtFixedRate(threadTask, initDelay, 1, MINUTES);
            // test !!! period, SECONDS
//            ScheduledFuture<?> notifyTask = scheduler.scheduleAtFixedRate(threadTask, initDelay, 3, SECONDS);
            System.out.println("Starting notifyTask");

            return notifyTask;
        }
}
