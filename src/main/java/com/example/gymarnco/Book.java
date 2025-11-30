// Corrected Book.java (change SportsType to BaseSport)

package com.example.gymarnco;

public class Book extends User {

    // CHANGE: Replace SportsType with BaseSport
    private BaseSport sportsType;
    private String date;
    private String time;

    // ... constructors ...

    // CHANGE: Use BaseSport in getter/setter
    public BaseSport getSportsType() { return sportsType; }
    public void setSportsType(BaseSport sportsType) { this.sportsType = sportsType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}