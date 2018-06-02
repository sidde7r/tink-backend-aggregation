package se.tink.analytics.lifecycle;

public interface Managed {
    void start() throws Exception;

    void stop() throws Exception;
}
