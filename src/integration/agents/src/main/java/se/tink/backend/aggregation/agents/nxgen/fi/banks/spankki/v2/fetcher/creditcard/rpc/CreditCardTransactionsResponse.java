package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities.CardTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CreditCardTransactionsResponse extends SpankkiResponse {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty private List<CardTransactionsEntity> cardTransactions;
    @JsonProperty private int pageAmount;

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
        logger.info(
                "tag={} {}",
                SpankkiConstants.LogTags.CREDIT_CARD_TRANSACTIONS,
                SerializationUtils.serializeToString(this));

        return Optional.empty();
    }
}
