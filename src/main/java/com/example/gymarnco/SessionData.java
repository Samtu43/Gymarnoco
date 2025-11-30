package com.example.gymarnco;

public class SessionData {

    private static Book booking;

    public static Book getBooking() {
        if (booking == null) {
            booking = new Book();
        }
        return booking;
    }

    public static void reset() {
        booking = new Book();
    }
}
