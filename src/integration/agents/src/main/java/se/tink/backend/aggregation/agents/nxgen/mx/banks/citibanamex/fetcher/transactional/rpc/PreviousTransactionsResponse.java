package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.entity.AccountMovementEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class PreviousTransactionsResponse extends BaseResponse {
    private List<AccountMovementEntity> accountMovement;
    private String continuityCode;
    private String moreMovements;

    @JsonIgnore
    public PreviousTransactionsResponse handleErrors() {
        if (!Strings.isNullOrEmpty(errmsg)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Error message: " + errmsg);
        }
        return this;
    }

    public List<Transaction> toTinkTransactions(String currency) {
        return accountMovement.stream()
                .filter(AccountMovementEntity::isValidTransaction)
                .map(accountMovementEntity -> accountMovementEntity.toTinkTransaction(currency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public String getContinuityCode() {
        return continuityCode;
    }
}
