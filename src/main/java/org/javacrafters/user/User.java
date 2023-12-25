package org.javacrafters.user;

import java.util.*;

public class User {

    private Long id;
    private String name;
    private String username;
    private final List<String> banks = new ArrayList<>();
    private final List<String> currency = new ArrayList<>();
    private int decimalPlaces = 2;
    private int notifyTime = 21;

    private boolean isNotifyOn = true;

    {
        currency.add("USD");
        banks.add("PB");
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

    public List<String> getBanks() {
        return banks;
    }

    public void removeBank(String bankLocalName) {

        banks.remove(bankLocalName);
    }

    public void addBank(String bankLocalName) {
        if (banks.contains(bankLocalName)) {
            return;
        }
        banks.add(bankLocalName);

    }

    public List<String> getCurrency() {
        return currency;
    }

    public void addCurrency(String name) {
        if (currency.contains(name)) {
            return;
        }
        currency.add(name);
    }

    public void removeCurrency(String name) {
        this.currency.remove(name);
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public int getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(int notifyTime) {
        this.notifyTime = notifyTime;
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

    public void setNotifyStatus(boolean status) {
        this.isNotifyOn = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name);
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
                ", banks=" + banks.toString() +
                ", currencies=" + currency.toString() +
                ", decimalPlaces=" + decimalPlaces +
                ", notifyTime=" + notifyTime +
                ", isNotifyOn=" + isNotifyOn +
                '}';
    }
}




