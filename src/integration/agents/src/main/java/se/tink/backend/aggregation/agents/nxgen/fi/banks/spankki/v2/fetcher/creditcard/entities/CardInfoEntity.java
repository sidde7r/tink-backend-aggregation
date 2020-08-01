package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CardInfoEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    public Optional<CreditCardAccount> toTinkCard() {
        logger.info(
                "tag={} {}",
                SpankkiConstants.LogTags.CREDIT_CARD,
                SerializationUtils.serializeToString(this));

        return Optional.empty();
    }
}
