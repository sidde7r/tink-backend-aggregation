package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30;

public interface NordeaConfiguration {
    String getBaseUrl();

    boolean isBusinessAgent();

    String getRedirectUri();

    String getClientId();
}
