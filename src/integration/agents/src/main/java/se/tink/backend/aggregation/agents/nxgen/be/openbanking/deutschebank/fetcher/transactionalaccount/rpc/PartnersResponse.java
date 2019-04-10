package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities.PartnersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartnersResponse {

    private List<PartnersEntity> partners;

    public String getNaturalFullName() {
        return Optional.ofNullable(partners).orElse(Collections.emptyList()).stream()
                .filter(PartnersEntity::isNatural)
                .map(PartnersEntity::getFullName)
                .findFirst()
                .orElse("");
    }
}
