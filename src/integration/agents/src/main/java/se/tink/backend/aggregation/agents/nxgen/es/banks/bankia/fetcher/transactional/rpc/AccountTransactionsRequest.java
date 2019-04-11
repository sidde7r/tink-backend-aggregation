package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.SearchCriteriaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountTransactionsRequest {
    @JsonProperty("identificadorCuenta")
    private AccountIdentifierEntity accountIdentifier;

    @JsonProperty("criteriosBusquedaCuenta")
    private SearchCriteriaEntity criteriaSearchAccount;

    @JsonProperty("idioma")
    private String language;

    private AccountTransactionsRequest(
            AccountIdentifierEntity accountIdentifier,
            SearchCriteriaEntity criteriaSearchAccount,
            String language) {
        this.accountIdentifier = accountIdentifier;
        this.criteriaSearchAccount = criteriaSearchAccount;
        this.language = language;
    }

    public static AccountTransactionsRequest create(
            AccountIdentifierEntity accountIdentifier,
            SearchCriteriaEntity criteriaSearchAccount,
            String language) {
        return new AccountTransactionsRequest(accountIdentifier, criteriaSearchAccount, language);
    }
}
