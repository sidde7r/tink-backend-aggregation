package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues.APP_VERSION;

import java.util.Optional;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface EuroInformationConfiguration extends ClientConfiguration {

    public String getUrl();

    public String getTarget();

    public String getAppVersion();

    public default String getAppVersionKey() {
        return APP_VERSION;
    }

    public default String getLoginSubpage() {
        return EuroInformationConstants.Url.LOGIN;
    }

    public default Optional<String> getInitEndpoint() {
        return Optional.of(EuroInformationConstants.Url.INIT);
    }
}
