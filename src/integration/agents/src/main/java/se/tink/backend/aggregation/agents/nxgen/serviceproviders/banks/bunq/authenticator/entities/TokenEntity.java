package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@RequiredArgsConstructor
@NoArgsConstructor
public class TokenEntity {
    private long id;
    private String created;
    private String updated;
    @NonNull private String token;
}
