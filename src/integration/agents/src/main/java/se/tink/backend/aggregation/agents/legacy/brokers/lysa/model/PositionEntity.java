package se.tink.backend.aggregation.agents.brokers.lysa.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PositionEntity {
    private double price;
    private double volume;
    private double worth;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getWorth() {
        return worth;
    }

    public void setWorth(double worth) {
        this.worth = worth;
    }
}
