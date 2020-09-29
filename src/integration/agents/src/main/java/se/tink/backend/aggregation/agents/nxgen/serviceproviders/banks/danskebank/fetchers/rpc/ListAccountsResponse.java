package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListAccountsResponse extends AbstractBankIdResponse {

    private String lastUpdated;
    private String languageCode;
    private List<AccountEntity> accounts;

    public String getLastUpdated() {
        return this.lastUpdated;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public List<AccountEntity> getAccounts() {
        return this.accounts != null ? this.accounts : Collections.emptyList();
    }

    public boolean isOwnAccount(String identifier) {
        return accounts.stream()
                .anyMatch(
                        accountEntity ->
                                accountEntity.getAccountNoExt().equalsIgnoreCase(identifier));
    }

    public AccountEntity findAccount(String identifier) {
        return accounts.stream()
                .filter(
                        accountEntity ->
                                accountEntity.getAccountNoExt().equalsIgnoreCase(identifier))
                .findFirst()
                .orElse(null);
    }
}
