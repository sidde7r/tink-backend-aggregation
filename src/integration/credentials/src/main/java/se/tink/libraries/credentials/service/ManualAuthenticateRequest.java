package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.user.rpc.User;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Setter
public class ManualAuthenticateRequest extends CredentialsRequest {
    @JsonProperty private boolean manual;

    /**
     * This constructor is used only for testing purposes. In case of real requests Jackson
     * reflection is used.
     */
    public ManualAuthenticateRequest(
            User user,
            Provider provider,
            Credentials credentials,
            UserAvailability userAvailability) {
        super(user, provider, credentials, userAvailability);
        this.manual = true;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.MANUAL_AUTHENTICATION;
    }

    /**
     * @deprecated use UserAvailability's userPresent or userAvailableForInteraction depending on
     *     what you need
     */
    @Override
    @Deprecated
    public boolean isManual() {
        return manual;
    }
}
