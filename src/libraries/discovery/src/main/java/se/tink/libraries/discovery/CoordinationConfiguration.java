package se.tink.libraries.discovery;

public interface CoordinationConfiguration {
    int getBaseSleepTimeMs();

    String getHosts();

    int getMaxRetries();
}
