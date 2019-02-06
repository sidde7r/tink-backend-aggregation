package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.ConfirmationInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationIdResponse {
    private ConfirmationInfoEntity confirmationInfo;

    public Optional<String> getId() {
        if (Objects.isNull(confirmationInfo)) {
            return Optional.empty();
        }

        return Optional.ofNullable(confirmationInfo.getId());
    }
}
