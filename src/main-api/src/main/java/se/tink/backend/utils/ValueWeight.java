package se.tink.backend.utils;

public class ValueWeight {
    private Double value;
    private Double weight;

    public ValueWeight(Double value, Double weight) {
        this.value = value;
        this.weight = weight;
    }

    public Double getValue() {
        return value;
    }

    public Double getWeight() {
        return weight;
    }
}
