package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.configuration;

public interface BpceConfiguration {

    String getAuthBaseUrl();

    String getAuthHeaderValue();

    String getAuthUserAgent();

    String getBranchId();

    String getClientId();

    String getClientSecret();

    String getIcgAuthBaseUrl();

    String getRsExAthBaseUrl();
}
