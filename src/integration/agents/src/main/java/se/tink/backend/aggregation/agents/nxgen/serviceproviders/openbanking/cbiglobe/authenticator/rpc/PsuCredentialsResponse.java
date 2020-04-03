package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@JsonObject
public class PsuCredentialsResponse {

    private String aspspProductCode;

    private List<CredentialsDetailResponse> credentialsDetails;

    public String getAspspProductCode() {
        return Optional.ofNullable(aspspProductCode)
                .orElseThrow(() -> new IllegalArgumentException("Product code must not be null"));
    }

    public List<CredentialsDetailResponse> getCredentialsDetails() {
        return Optional.ofNullable(credentialsDetails)
                .orElseThrow(
                        () -> new IllegalArgumentException("Credentials details must not be null"));
    }
}
