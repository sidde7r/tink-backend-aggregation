package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CardInfoEntity {
    @JsonProperty private String productCode;
    @JsonProperty private String cardName;
    @JsonProperty private String debitCardNr;
    @JsonProperty private String contractNr;
    @JsonProperty private String cardStatus;
    @JsonProperty private String embossName;
    @JsonProperty private String expire;
    @JsonProperty private String memberNr;
    @JsonProperty private String accountNr;
    @JsonProperty private String withDrawLimit;
    @JsonProperty private String ecommerceLimit;
    @JsonProperty private String purchaseLimit;
    @JsonProperty private String authorizationRegionId;
    @JsonProperty private Boolean vbv;
    @JsonProperty private Boolean cardRenewal;
    @JsonProperty private Boolean pinReorderOk;
    @JsonProperty private Boolean modifyLimits;
    @JsonProperty private Boolean modifyRegions;
    @JsonProperty private Boolean movableToAccount;
    @JsonProperty private String type;
    @JsonProperty private Boolean transferable;
    @JsonProperty private String cardShortName;
    @JsonProperty private Boolean paymentFreeMonthsChangeAllowed;
    @JsonProperty private Boolean showLimits;

    @JsonIgnore
    private static final AggregationLogger logger = new AggregationLogger(CardsEntity.class);

    @JsonIgnore
    public Optional<CreditCardAccount> toTinkCard() {
        logger.infoExtraLong(
                SerializationUtils.serializeToString(this), SpankkiConstants.LogTags.CREDIT_CARD);

        return Optional.empty();
    }
}
