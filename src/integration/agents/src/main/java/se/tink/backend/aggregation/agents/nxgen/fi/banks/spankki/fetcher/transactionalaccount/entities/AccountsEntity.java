package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsEntity {
    @JsonIgnore
    private static final AggregationLogger LOGGER = new AggregationLogger(AccountsEntity.class);

    private String accountNumber;
    private String accountId;
    private String notificationAccountId;
    private String accountNickname;
    private String currency;
    private String availableAmount;
    private double balance;
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

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(
                        convertAccountType(), accountNumber, new Amount(currency, balance))
                .setAccountNumber(accountNumber)
                .setName(accountNickname)
                .setBankIdentifier(accountId)
                .build();
    }

    private AccountTypes convertAccountType() {
        SpankkiConstants.AccountType type = SpankkiConstants.AccountType.toAccountType(accountType);

        if (type == SpankkiConstants.AccountType.UNKOWN) {
            // until we have some more knowledge set everything to checking and log to improve
            LOGGER.info(
                    String.format(
                            "%s - Found unhandled account Nickname [%s], account type [%s], account type name [%s]",
                            SpankkiConstants.LogTags.LOG_TAG_ACCOUNT_TYPE,
                            accountNickname,
                            accountType,
                            accountTypeName));

            return AccountTypes.CHECKING;
        }

        return type.getTinkType();
    }
}
