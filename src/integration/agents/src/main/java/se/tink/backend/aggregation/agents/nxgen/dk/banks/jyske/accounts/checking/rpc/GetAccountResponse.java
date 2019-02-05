package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.AccountBriefEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountResponse {
    private List<AccountEntity> accounts;
    private List<Object> mastercardAgreements;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public List<AccountBriefEntity> getAccountsBrief() {
        return accounts.stream()
                       .map(AccountEntity::toAccountBriefEntity)
                       .collect(Collectors.toList());
    }
}
