package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Regex;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty private String accountNumber;
    @JsonProperty private String accountId;
    @JsonProperty private String notificationAccountId;
    @JsonProperty private String accountNickname;
    @JsonProperty private String currency;
    @JsonProperty private String availableAmount;
    @JsonProperty private BigDecimal balance;
    @JsonProperty private boolean payableAccount;
    @JsonProperty private boolean transferable;
    @JsonProperty private String accountGroup;
    @JsonProperty private boolean defaultAccount;
    @JsonProperty private boolean reservations;
    @JsonProperty private String interestRate;
    @JsonProperty private String accountType;
    @JsonProperty private String accountTypeName;
    @JsonProperty private String interestMargin;
    @JsonProperty private String capitalization;
    @JsonProperty private String minInterestRate;
    @JsonProperty private String maxInterestRate;
    @JsonProperty private String referenceInterestName;
    @JsonProperty private String referenceInterestValue;
    @JsonProperty private boolean showAccount;
    @JsonProperty private String reservationAmount;
    @JsonProperty private String bonusPaymentAccount;
    @JsonProperty private String accountOwnerName;
    @JsonProperty private String accountCoOwnerName;
    @JsonProperty private String bbanFormatted;
    @JsonProperty private String bic;
    @JsonProperty private boolean owner;
    @JsonProperty private boolean allowedAsBenefit;
    @JsonProperty private boolean fundRedemptionAllowed;
    @JsonProperty private boolean fundSubscriptionAllowed;
    @JsonProperty private String sortName;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(SpankkiConstants.ACCOUNT_TYPE_MAPPER, accountType)
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(balance, currency)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountNickname)
                                .addIdentifier(
                                        new FinnishIdentifier(
                                                accountNumber.replaceAll(Regex.WHITE_SPACE, "")))
                                .build())
                .addHolderName(accountOwnerName)
                .setApiIdentifier(accountId)
                .build();
    }
}
