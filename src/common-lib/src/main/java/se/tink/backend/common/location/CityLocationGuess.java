package se.tink.backend.common.location;


public class CityLocationGuess extends LocationGuess {

    private String city;

    public CityLocationGuess(LocationGuessType type) {
        super(LocationResolution.CITY, type);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
