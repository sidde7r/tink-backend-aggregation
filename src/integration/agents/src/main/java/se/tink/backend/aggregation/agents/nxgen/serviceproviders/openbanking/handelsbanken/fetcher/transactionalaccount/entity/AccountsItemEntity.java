package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
@Getter
public class AccountsItemEntity {

    private String accountId;
    private String bban;
    private String ownerName;
    private String iban;
    private String accountType;
    private String name;
    private String currency;
    private String bic;
    private String clearingNumber;

    public Optional<TransactionalAccount> toTinkAccount(
            TransactionalAccountType type, AccountDetailsResponse accountDetails) {

        Optional<BalancesItemEntity> availableBalance = getAvailableBalance(accountDetails);
        if (!availableBalance.isPresent()) {
            log.warn(HandelsbankenBaseConstants.ExceptionMessages.BALANCE_NOT_FOUND);
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAmount(availableBalance.get())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getBbanWithoutClearing())
                                .withAccountNumber(getAccountNumberWithClearing())
                                .withAccountName(Optional.ofNullable(name).orElse(accountType))
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SE,
                                                clearingNumber.concat(bban)))
                                .build())
                .addHolderName(ownerName)
                .setApiIdentifier(accountId)
                .build();
    }

    public Optional<BalancesItemEntity> getAvailableBalance(AccountDetailsResponse accountDetails) {
        return accountDetails.getBalances().stream()
                .filter(BalancesItemEntity::isBalance)
                .findFirst();
    }

    @JsonIgnore
    private String getBbanWithoutClearing() {
        // 9 is the documented max length of an shb account number, anything longer we would have to
        // look closer at.
        if (getBban().length() > 9) {
            throw new IllegalStateException("Unexpected bban: " + getBban());
        }
        return getBban();
    }

    // Use BBAN as getting the bban from iban is not a static operation as there can be up to 30
    // characters in the bban part of the iban, if we have no clearing number, assume bban has it.
    @JsonIgnore
    private String getAccountNumberWithClearing() {
        if (Strings.isNullOrEmpty(clearingNumber)) {
            return getBban();
        }

        return clearingNumber + "-" + getBbanWithoutClearing();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAmount(BalancesItemEntity balance) {
        return new ExactCurrencyAmount(
                balance.getAmountEntity().getContent(), balance.getAmountEntity().getCurrency());
    }
}
