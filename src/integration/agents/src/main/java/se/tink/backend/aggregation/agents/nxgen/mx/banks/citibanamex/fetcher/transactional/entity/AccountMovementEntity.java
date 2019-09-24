package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class AccountMovementEntity {

    @JsonProperty("CompleteDate")
    private String completeDate;

    private String amount;
    private String authorizationNumber;
    private String concept;
    private String date;
    private String cepIndicator;

    @JsonIgnore
    public boolean isValidTransaction() {
        return !StringUtils.isAnyEmpty(amount, date, concept);
    }

    @JsonIgnore
    public Optional<Transaction> toTinkTransaction(String currency) {
        return Optional.of(
                Transaction.builder()
                        .setAmount(getAmount(currency))
                        .setDate(DateUtils.parseDate(completeDate))
                        .setDescription(concept)
                        .build());
    }

    @JsonIgnore
    private ExactCurrencyAmount getAmount(String currency) {
        return ExactCurrencyAmount.of(
                new BigDecimal(
                        StringUtils.removePattern(amount, CitiBanaMexConstants.AMOUNT_REGEX)),
                currency);
    }
}
