package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequestAccessEntity {

    private List<AccountAccessEntity> accounts;

    public ConsentRequestAccessEntity(List<String> ibans) {
        this.accounts = ibans.stream().map(AccountAccessEntity::new).collect(Collectors.toList());
    }
}
