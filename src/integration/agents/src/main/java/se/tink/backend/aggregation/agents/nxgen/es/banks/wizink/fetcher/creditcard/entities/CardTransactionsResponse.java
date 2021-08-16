package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@Slf4j
@JsonObject
public class CardTransactionsResponse extends BaseResponse {

    @JsonProperty("listaMovimiento")
    private List<CardTransactionEntity> cardTransactions;

    @JsonProperty("more90Days")
    private boolean hasTransactionsOlderThan90Days;

    @JsonProperty("otp")
    private SessionEntity sessionEntity;

    private String mobilePhone;
    private boolean haveMore;

    public boolean canFetchTransactionsOlderThan90Days() {
        return hasTransactionsOlderThan90Days;
    }

    public SessionEntity getSessionEntity() {
        return sessionEntity;
    }

    public boolean isHaveMore() {
        return haveMore;
    }

    @JsonIgnore
    public List<AggregationTransaction> getTransactions(CreditCardAccount account) {
        return Optional.ofNullable(cardTransactions).orElse(getCardTransactionsFromStorage(account))
                .stream()
                .map(cardTrEntity -> cardTrEntity.toTinkTransaction(account))
                .collect(Collectors.toList());
    }

    private List<CardTransactionEntity> getCardTransactionsFromStorage(CreditCardAccount account) {
        return account.getFromTemporaryStorage(
                        StorageKeys.CARD_TRANSACTIONS_LIST,
                        new TypeReference<List<CardTransactionEntity>>() {})
                .orElseGet(
                        () -> {
                            log.info("No card transactions found");
                            return Collections.emptyList();
                        });
    }
}
