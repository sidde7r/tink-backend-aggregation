package se.tink.backend.common.location;


public class LocationGuess {
    private LocationGuessType type;
    private LocationResolution resolution;
    private float probability;

    public LocationGuess (LocationResolution resolution, LocationGuessType type) {
        this.resolution = resolution;
        this.type = type;
    }

    public LocationGuessType getType() {
        return type;
    }

    public LocationResolution getResolution() {
        return resolution;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

    public void setType(LocationGuessType type) {
        this.type = type;
    }
}
