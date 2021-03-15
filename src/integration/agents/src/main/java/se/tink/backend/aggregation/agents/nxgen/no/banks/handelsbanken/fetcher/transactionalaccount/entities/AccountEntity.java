package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
@Getter
public class AccountEntity {
    private String id;
    private String accountNumber;
    private String bban;
    private String iban;
    private String customerRole;
    private OwnerEntity owner;
    private String displayName;
    private PropertiesEntity properties;
    private AccountBalanceEntity accountBalance;
    private RightsEntity rights;
    private HashMap<String, LinkEntity> links;

    @JsonIgnore
    private static final List<AccountTypes> TRANSACTIONAL_ACCOUNT_TYPES =
            ImmutableList.of(AccountTypes.CHECKING, AccountTypes.SAVINGS);

    private String getTransactionUrl() {
        return links.get(HandelsbankenNOConstants.Tags.TRANSACTIONS).getHref();
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return HandelsbankenNOConstants.AccountType.ACCOUNT_TYPE_MAPPER.isOneOf(
                properties.getType(), TRANSACTIONAL_ACCOUNT_TYPES);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        AccountTypes accountType = getTinkAccountType();

        if (TransactionalAccountType.isNotTransactionalAccount(accountType)) {
            return Optional.empty();
        }

        TransactionalAccountType transactionalAccountType =
                TransactionalAccountType.from(accountType).orElse(null);
        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance(accountBalance.getAvailableBalance())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(displayName)
                                .addIdentifier(new OtherIdentifier(accountNumber))
                                .build())
                .setApiIdentifier(getTransactionUrl())
                .addHolderName(owner.getName())
                .build();
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        return HandelsbankenNOConstants.AccountType.ACCOUNT_TYPE_MAPPER
                .translate(properties.getType())
                .orElse(AccountTypes.OTHER);
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance(double balance) {
        String currency = properties.getCurrencyCode();

        if (Strings.isNullOrEmpty(currency)) {
            log.warn("Handelsbanken Norway: No currency for account found. Defaulting to NOK.");
            return ExactCurrencyAmount.of(balance, "NOK");
        }

        return ExactCurrencyAmount.of(balance, currency);
    }
}
