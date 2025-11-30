package com.example.gymarnco;

public enum SportsType {

    BASKETBALL_INDOOR("Basketball", "Indoor", 250),
    BASKETBALL_OUTDOOR("Basketball", "Outdoor", 200),

    VOLLEYBALL_INDOOR("Volleyball", "Indoor", 250),
    VOLLEYBALL_BEACH("Volleyball", "Beach Court", 225),

    BADMINTON_A("Badminton", "Court A", 150),
    BADMINTON_B("Badminton", "Court B", 150),

    JOGGING_TRACK("Jogging Track", "Track", 50),

    SEPAK_TAKRAW("Sepak Takraw", "Traditional Court", 200);

    private final String type;
    private final String court;
    private final double amount;

    SportsType(String type, String court, double amount) {
        this.type = type;
        this.court = court;
        this.amount = amount;
    }

    public String getType() { return type; }
    public String getCourt() { return court; }
    public double getAmount() { return amount; }
}
