
package org.javacrafters.scheduler;

import org.javacrafters.ChatBot;
import org.javacrafters.user.User;

import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;


public class Scheduler {

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
//    protected final ExecutorService service = Executors.newFixedThreadPool(2);

        public ScheduledFuture<?> schedule(ChatBot bot, User user, int hourToNotify) {

//            hourToNotify = 9;
            int initDelay = 1;
            int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int mins = Calendar.getInstance().get(Calendar.MINUTE);
//            initDelay = curHour > hourToNotify ? ((24 - curHour + hourToNotify) * 60 + mins) : ((hourToNotify - curHour) * 60 - mins);


            final Runnable threadTask = new Runnable() {

                public void run() {
                    System.out.println(".");
                    if (user.isNotifyOn()) {
                        System.out.println("Notified user: " + user.getId() +" "+ user.getName());
//                        bot.userNotify(user);
                    }
                }
            };

//            ScheduledFuture<?> notifyTask = scheduler.scheduleAtFixedRate(threadTask, initDelay, 24, MINUTES);
            // test
            ScheduledFuture<?> notifyTask = scheduler.scheduleAtFixedRate(threadTask, initDelay, 3, SECONDS);
            System.out.println("Starting notifyTask");

            return notifyTask;
        }
}
