package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusHolderNameDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusStringDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BelfiusProduct implements GeneralAccountEntity {

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
    // ------- Logged for credit card
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
    // ------- End logged for credit card

    private Optional<Amount> getAmount() {
        return BelfiusStringUtils.parseStringToAmount(this.amount);
    }

    private Optional<Amount> getBalance() {
        Optional<Amount> amount = getAmount();
        Optional<Amount> available = getAvailableAmount();
        if (amount.isPresent() && available.isPresent()) {
            if (available.get().isLessThan(amount.get().doubleValue())) {
                return available;
            }
        }
        return amount.isPresent() ? amount : available;
    }

    public Amount getAvailableBalance() {
        Optional<Amount> amount = getAmount();
        Optional<Amount> available = getAvailableAmount();
        if (amount.isPresent() && available.isPresent()) {
            if (available.get().isGreaterThan(amount.get().doubleValue())) {
                return available.get();
            }
        }
        return amount.orElseThrow(IllegalArgumentException::new);
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
        return getBalance()
                .map(
                        parsedAmount ->
                                TransactionalAccount.builder(
                                                getAccountType(), this.extIntAccount, parsedAmount)
                                        .setAccountNumber(this.numberAccount)
                                        .setName(this.denominationDescription)
                                        .setBankIdentifier(key)
                                        .setHolderName(this.holderName)
                                        .addIdentifier(
                                                new SepaEurIdentifier(
                                                        this.numberAccount.replace(" ", "")))
                                        .build())
                .orElse(null);
    }

    public boolean isCreditCard() {
        return getAccountType() == AccountTypes.CREDIT_CARD;
    }

    public CreditCardAccount toCreditCardAccount(String key) {
        return getAmount()
                .flatMap(
                        amount ->
                                parseAvailableForCreditCard()
                                        .map(
                                                availableAmount ->
                                                        CreditCardAccount.builder(
                                                                        this.numberAccount,
                                                                        amount,
                                                                        availableAmount)
                                                                .setAccountNumber(
                                                                        this.numberAccount)
                                                                .setBankIdentifier(key)
                                                                .build()))
                .orElse(null);
    }

    private Optional<Amount> parseAvailableForCreditCard() {
        Optional<Amount> parsedAvailableAmount = getAvailableAmount();
        if (parsedAvailableAmount.isPresent()) return parsedAvailableAmount;
        return BelfiusStringUtils.parseStringToAmount(this.effectiveAvailableCard);
    }

    private Optional<Amount> getAvailableAmount() {
        return BelfiusStringUtils.parseStringToAmount(this.available);
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
