package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration;

public abstract class NordeaConfiguration {
    public abstract String getBaseUrl();

    public abstract String getFirstApiVersion();

    public abstract String getSecondApiVersion();
}
