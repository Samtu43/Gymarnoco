package com.example.gymarnco;

public class SessionData {

    private static BaseSport selectedBaseSport;

    public static BaseSport getSelectedBaseSport() {
        return selectedBaseSport;
    }

    public static void setSelectedBaseSport(BaseSport sport) {
        selectedBaseSport = sport;
    }

    public static void reset() {
        selectedBaseSport = null;
    }
}
