package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.UserInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@SuppressWarnings("unused")
@JsonObject
public class MeResponse {
    private UserInfoEntity userInfo;

    public IdentityData toTinkIdentity() {
        return IdentityData.builder()
                .addFirstNameElement(userInfo.getFirstName())
                .addSurnameElement(userInfo.getLastName())
                .setDateOfBirth(userInfo.getBirthDate())
                .build();
    }
}
