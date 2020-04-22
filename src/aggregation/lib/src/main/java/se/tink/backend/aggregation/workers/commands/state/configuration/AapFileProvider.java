package se.tink.backend.aggregation.workers.commands.state.configuration;

public interface AapFileProvider {
    String getAapFilePath(String providerName);
}
