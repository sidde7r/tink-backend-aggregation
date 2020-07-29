package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities.CardTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CreditCardTransactionsResponse extends SpankkiResponse {
    @JsonProperty private List<CardTransactionsEntity> cardTransactions;
    @JsonProperty private int pageAmount;

    @JsonIgnore
    private static final AggregationLogger logger =
            new AggregationLogger(CreditCardTransactionsResponse.class);

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setCardTransactions(List<CardTransactionsEntity> cardTransactions) {
        this.cardTransactions = cardTransactions;
    }

    public List<CardTransactionsEntity> getCardTransactions() {
        return cardTransactions;
    }

    @JsonIgnore
    public int getPageAmount() {
        return pageAmount;
    }

    @JsonIgnore
    public Optional<CreditCardTransaction> toTinkCardTransactions() {
        logger.infoExtraLong(
                SerializationUtils.serializeToString(this),
                SpankkiConstants.LogTags.CREDIT_CARD_TRANSACTIONS);

        return Optional.empty();
    }
}
