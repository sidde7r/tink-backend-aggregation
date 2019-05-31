package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountAttributesEntity {

    private AccountIdentifierEntity identifier;

    private String type;

    public AccountIdentifierEntity getIdentifier() {
        return Optional.ofNullable(identifier)
                .orElseThrow(() -> new IllegalStateException("Missing Identifier"));
    }
}
