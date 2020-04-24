package se.tink.backend.aggregation.workers.commands.state.configuration;

import java.util.Set;

public interface AapFileProvider {
    Set<String> getAapFilePaths(String providerName);
}
