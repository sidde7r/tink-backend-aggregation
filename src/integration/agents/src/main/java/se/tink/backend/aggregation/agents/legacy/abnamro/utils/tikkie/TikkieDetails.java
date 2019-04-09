package se.tink.backend.aggregation.agents.abnamro.utils.tikkie;

/** Small utility class for storing name and message parsed out from a Tikkie transaction */
public class TikkieDetails {
    private String name;
    private String message;

    public TikkieDetails(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}
