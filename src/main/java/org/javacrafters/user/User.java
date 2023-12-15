package org.javacrafters.user;

import org.javacrafters.banking.Bank;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.networkclient.NetworkStreamReader;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class User {

    private Long id;
    private String name;
    private String username;
    private Bank bank;
    private final List<String> currencies = new ArrayList<>();
    private int numOfDigits = 2;
    private int notifyTime = 21;
    private ScheduledFuture<?> scheduledTask;
    private boolean isNotifyOn = true;

    {
        currencies.add("USD");
        bank = new PrivatBank(new NetworkStreamReader());
    }
    public User() {
    }

    public User(Long id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
   }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void addCurrency(String name) {
        if (currencies.contains(name)) {
            return;
        }
        this.currencies.add(name);
    }
    public void removeCurrency(String name) {
        this.currencies.remove(name);
    }

    public int getNumOfDigits() {
        return numOfDigits;
    }

    public void setNumOfDigits(int numOfDigits) {
        this.numOfDigits = numOfDigits;
    }

    public int getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(int notifyTime) {
        this.notifyTime = notifyTime;
    }
    public ScheduledFuture<?> getScheduledTask() {
        return this.scheduledTask;
    }

    public void setScheduledTask(ScheduledFuture<?> scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public boolean isNotifyOn() {
        return isNotifyOn;
    }

    public void setNotifyOn() {
        this.isNotifyOn = true;
    }
    public void setNotifyOff() {
        this.isNotifyOn = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", banks=" + bank.toString() +
                ", currencies=" + currencies.toString() +
                ", numOfDigits=" + numOfDigits +
                ", notifyTime=" + notifyTime +
                ", isNotifyEnabled=" + isNotifyOn +
                ", scheduledTask=" + scheduledTask.toString() +
                '}';
    }
}




