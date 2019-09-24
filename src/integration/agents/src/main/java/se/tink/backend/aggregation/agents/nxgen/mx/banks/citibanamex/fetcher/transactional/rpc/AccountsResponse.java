package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.entity.CategoriesEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse extends BaseResponse {

    @JsonProperty("Categories")
    private List<CategoriesEntity> categories;

    private String errorCde;
    private String insights;

    public List<TransactionalAccount> toTransactionalAccounts(String holderName) {
        return categories.stream()
                .map(categoriesEntity -> categoriesEntity.toTransactionalAccounts(holderName))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public AccountsResponse handleErrors() {
        if (!Strings.isNullOrEmpty(errmsg)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return this;
    }
}
