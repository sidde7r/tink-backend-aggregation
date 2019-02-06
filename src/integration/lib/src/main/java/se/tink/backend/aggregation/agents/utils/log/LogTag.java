package se.tink.backend.aggregation.agents.utils.log;

public class LogTag {
    private final String tag;

    private LogTag(String tag) {
        this.tag = tag;
    }

    public static LogTag from(String tag) {
        return new LogTag(tag);
    }

    @Override
    public String toString() {
        return tag;
    }

    public String concat(String message) {
        return String.format("%s - %s", tag, message);
    }
}
