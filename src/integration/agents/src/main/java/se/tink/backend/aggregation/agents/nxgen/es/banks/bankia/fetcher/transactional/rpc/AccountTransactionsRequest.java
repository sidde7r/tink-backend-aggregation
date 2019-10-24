package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.PaginationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.SearchCriteriaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountTransactionsRequest {
    @JsonProperty("identificadorCuenta")
    private AccountIdentifierEntity accountIdentifier;

    @JsonProperty("criteriosBusquedaCuenta")
    private SearchCriteriaEntity criteriaSearchAccount;

    @JsonProperty("idioma")
    private String language;

    @JsonProperty("datosRellamada")
    private PaginationDataEntity paginationData;

    @JsonProperty("sinPaginar")
    private Boolean withoutPagination;

    private AccountTransactionsRequest(
            AccountIdentifierEntity accountIdentifier,
            SearchCriteriaEntity criteriaSearchAccount,
            String language,
            PaginationDataEntity paginationData) {
        this.accountIdentifier = accountIdentifier;
        this.criteriaSearchAccount = criteriaSearchAccount;
        this.language = language;
        this.paginationData = paginationData;
        if (Objects.isNull(paginationData)) {
            withoutPagination = Boolean.FALSE;
        }
    }

    public static AccountTransactionsRequest create(
            AccountIdentifierEntity accountIdentifier,
            SearchCriteriaEntity criteriaSearchAccount,
            String language,
            PaginationDataEntity paginationData) {
        return new AccountTransactionsRequest(
                accountIdentifier, criteriaSearchAccount, language, paginationData);
    }
}
