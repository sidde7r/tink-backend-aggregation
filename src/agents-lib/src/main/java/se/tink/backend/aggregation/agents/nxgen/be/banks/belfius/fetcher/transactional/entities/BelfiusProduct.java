package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusHolderNameDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusStringDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

@JsonObject
public class BelfiusProduct implements GeneralAccountEntity{

    private static final AggregationLogger LOGGER = new AggregationLogger(BelfiusProduct.class);

    @JsonProperty("lb_orderingaccount")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String orderingAccount;

    @JsonProperty("lb_typeaccount")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String typeAccount;

    @JsonProperty("lb_denominationcode")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String denominationCode;

    @JsonProperty("lb_numberaccount")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String numberAccount;

    @JsonProperty("lb_denominationdescription")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String denominationDescription;

    @JsonProperty("lb_ext_int_acc")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String extIntAccount;

    @JsonProperty("lb_amount")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String amount;

    @JsonProperty("lb_effectiveavailablecard")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String effectiveAvailableCard;

    @JsonProperty("lb_available")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String available;

    @JsonProperty("lb_holdername")
    @JsonDeserialize(using = BelfiusHolderNameDeserializer.class)
    private HolderName holderName;

    // Fields below are added for logging purposes. Can be removed if not used.
    //------- Logged for credit card
    @JsonProperty("lb_creditcardactionallowed")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String creditcardActionAllowed;

    @JsonProperty("mlb_NbRecyclage")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String nbRecyclage;

    @JsonProperty("lb_totalclosurecard")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String totalClosureCard;

    @JsonProperty("mlb_ZoomItAllowed")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String zoomItAllowed;
    //------- End logged for credit card


    private Optional<Amount> getAmount() {
        return BelfiusStringUtils.parseStringToAmount(this.amount);
    }

    public boolean isTransactionalAccount() {
        switch (getAccountType()) {
        case CHECKING:
        case SAVINGS:
            return true;
        default:
            return false;
        }
    }

    public AccountTypes getAccountType() {
        return BelfiusConstants.ACCOUNT_TYPES.getOrDefault(this.typeAccount, AccountTypes.OTHER);
    }

    public TransactionalAccount toTransactionalAccount(String key) {
        return getAmount().map(amount ->
                TransactionalAccount.builder(getAccountType(), this.extIntAccount, amount)
                        .setAccountNumber(this.numberAccount)
                        .setName(this.denominationDescription)
                        .setBankIdentifier(key)
                        .setHolderName(this.holderName)
                        .addIdentifier(new SepaEurIdentifier(this.numberAccount.replace(" ", "")))
                        .build()
        ).orElse(null);
    }

    public boolean isCreditCard() {
        return getAccountType() == AccountTypes.CREDIT_CARD;
    }

    public CreditCardAccount toCreditCardAccount(String key) {
        return getAmount().flatMap(amount ->
                parseAvailable().map(availableAmount ->
                        CreditCardAccount.builder(this.numberAccount, amount, availableAmount)
                                .setAccountNumber(this.numberAccount)
                                .setBankIdentifier(key)
                                .build()
                )

        ).orElse(null);
    }

    private Optional<Amount> parseAvailable() {
        Optional<Amount> availableAmount = BelfiusStringUtils.parseStringToAmount(this.available);
        if (availableAmount.isPresent()) {
            return availableAmount;
        }
        return BelfiusStringUtils.parseStringToAmount(this.effectiveAvailableCard);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("orderingAccount", this.orderingAccount)
                .append("typeAccount", this.typeAccount)
                .append("denominationCode", this.denominationCode)
                .append("numberAccount", this.numberAccount)
                .append("denominationDescription", this.denominationDescription)
                .append("extIntAccount", this.extIntAccount)
                .append("amount", this.amount)
                .append("effectiveAvailableCard", this.effectiveAvailableCard)
                .append("available", this.available)
                .append("creditcardActionAllowed", this.creditcardActionAllowed)
                .append("holderName", this.holderName)
                .append("nbRecyclage", this.nbRecyclage)
                .append("totalClosureCard", this.totalClosureCard)
                .append("zoomItAllowed", this.zoomItAllowed)
                .toString();
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        SepaEurIdentifier bi = new SepaEurIdentifier(this.extIntAccount.replace(" ", ""));
        return bi;
    }

    @Override
    public String generalGetBank() {
        return "";
    }

    @Override
    public String generalGetName() {
        return this.holderName.toString();
    }
}
