package org.javacrafters.scheduler;

import org.javacrafters.core.ChatBot;
import org.javacrafters.user.User;

import java.util.Calendar;
import java.util.Collection;


public class Scheduler2 {

        public Runnable schedule(ChatBot bot) {
            return new Runnable() {
                public void run() {
                    System.out.println("Starting" + Thread.currentThread().getName());
                    while (true) {
                        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

                        if (bot.getUsers() != null && !bot.getUsers().isEmpty()) {
                            Collection<User> users = bot.getUsers().values();
                            for (User user : users) {
                                  // checks for notify only once per hour
//                                if (user.isNotifyOn() && user.getNotifyTime() == hour && hour != user.getLastNotifyTime()) {
                                    // for testing only
//                                if (user.isNotifyOn()) {
                                    bot.userNotify(user);
//                                    user.setLastNotifyTime(hour);
//                                }
                            }
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            };

        }

    }
