package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@JsonObject
public class AccountHoldersResponse {

    @JsonProperty("EE_O_Consulta")
    private AccountHoldersQueryEntity accountHoldersQueryEntity;

    public AccountHoldersQueryEntity getAccountHoldersQueryEntity() {
        return accountHoldersQueryEntity;
    }

    public List<Party> getParties() {
        if (accountHoldersQueryEntity != null
                && accountHoldersQueryEntity.getAccountHoldersEntity() != null
                && accountHoldersQueryEntity.getAccountHoldersEntity().getAccountHolderEntityList()
                        != null) {
            return accountHoldersQueryEntity.getAccountHoldersEntity().getAccountHolderEntityList()
                    .stream()
                    .map(AccountHolderEntity::toTinkParty)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
