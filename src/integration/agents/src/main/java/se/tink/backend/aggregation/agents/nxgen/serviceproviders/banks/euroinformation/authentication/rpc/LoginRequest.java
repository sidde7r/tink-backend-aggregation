package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class LoginRequest extends AbstractForm {
    public LoginRequest(
            String username,
            String password,
            String appVersionKey,
            String appVersion,
            String target) {
        this.put(EuroInformationConstants.RequestBodyValues.USER, username);
        this.put(EuroInformationConstants.RequestBodyValues.PASSWORD, password);
        this.put(appVersionKey, appVersion);
        this.put(EuroInformationConstants.RequestBodyValues.TARGET, target);
        this.put(EuroInformationConstants.RequestBodyValues.WS_VERSION, "2");
        this.put(
                EuroInformationConstants.RequestBodyValues.MEDIA,
                EuroInformationConstants.RequestBodyValues.MEDIA_VALUE);
    }
}
