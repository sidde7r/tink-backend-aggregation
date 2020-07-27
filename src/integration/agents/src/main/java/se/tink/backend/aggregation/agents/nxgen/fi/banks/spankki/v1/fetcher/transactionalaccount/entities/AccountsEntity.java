package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiConstants.Regex;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsEntity {
    private String accountNumber;
    private String accountId;
    private String notificationAccountId;
    private String accountNickname;
    private String currency;
    private String availableAmount;
    private BigDecimal balance;
    private boolean payableAccount;
    private boolean transferable;
    private String accountGroup;
    private boolean defaultAccount;
    private boolean reservations;
    private String interestRate;
    private String accountType;
    private String accountTypeName;
    private String interestMargin;
    private String capitalization;
    private String minInterestRate;
    private String maxInterestRate;
    private String referenceInterestName;
    private String referenceInterestValue;
    private boolean showAccount;
    private String reservationAmount;
    private String bonusPaymentAccount;
    private String accountOwnerName;
    private String accountCoOwnerName;
    private String bbanFormatted;
    private String bic;
    private boolean owner;
    private boolean allowedAsBenefit;
    private boolean fundRedemptionAllowed;
    private boolean fundSubscriptionAllowed;
    private String sortName;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        SpankkiConstants.ACCOUNT_TYPE_MAPPER,
                        accountType,
                        TransactionalAccountType.OTHER)
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
