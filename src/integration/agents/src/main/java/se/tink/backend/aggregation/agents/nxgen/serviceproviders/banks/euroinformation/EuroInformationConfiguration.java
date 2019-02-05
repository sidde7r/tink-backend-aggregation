package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import java.util.Optional;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues.APP_VERSION;

public interface EuroInformationConfiguration {

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
