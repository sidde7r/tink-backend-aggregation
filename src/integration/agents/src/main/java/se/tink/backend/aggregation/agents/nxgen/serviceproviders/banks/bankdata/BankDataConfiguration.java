package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface BankDataConfiguration extends ClientConfiguration {

    String getAuthHost();

    String getHost();

    String getUserAgent();

    String getBuildNumber();

    String getApiKey();

    String getAppVersion();

    String getReferer();

    String getRedirectUri();
}
