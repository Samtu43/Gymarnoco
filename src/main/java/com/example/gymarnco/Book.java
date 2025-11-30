package com.example.gymarnco;

public class Book extends User {

    private SportsType sportsType;
    private String date;   // "2025-01-10"
    private String time;   // "08:00 AM - 09:00 AM"

    public Book() {}

    public Book(int id, String name, String phoneNumber, String emailAddress) {
        super(id, name, phoneNumber, emailAddress);
    }

    public SportsType getSportsType() { return sportsType; }
    public void setSportsType(SportsType sportsType) { this.sportsType = sportsType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
