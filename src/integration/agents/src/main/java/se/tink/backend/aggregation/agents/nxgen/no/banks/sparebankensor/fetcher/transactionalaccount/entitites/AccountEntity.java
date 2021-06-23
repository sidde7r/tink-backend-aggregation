package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
public class AccountEntity {
    private String id;
    private String displayName;
    private OwnerEntity owner;
    private PropertiesEntity properties;
    private AccountBalanceEntity accountBalance;
    private String iban;
    private String bban;

    @Getter private Map<String, LinkEntity> links;

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return SparebankenSorConstants.Accounts.ACCOUNT_TYPE_MAPPER.isOneOf(
                properties.getType(),
                ImmutableList.of(AccountTypes.CHECKING, AccountTypes.SAVINGS));
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(
                        getTinkAccountType(), id, getBalance(accountBalance.getAvailableBalance()))
                .setAccountNumber(id)
                .setName(displayName)
                .setHolderName(new HolderName(owner.getName()))
                .setBankIdentifier(id)
                .addIdentifiers(Arrays.asList(new IbanIdentifier(iban), new BbanIdentifier(bban)))
                .putInTemporaryStorage(
                        SparebankenSorConstants.Storage.TEMPORARY_STORAGE_LINKS, links)
                .build();
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        return SparebankenSorConstants.Accounts.ACCOUNT_TYPE_MAPPER
                .translate(properties.getType())
                .orElse(AccountTypes.OTHER);
    }

    @JsonIgnore
    public boolean isLoanAccount() {
        return SparebankenSorConstants.Accounts.ACCOUNT_TYPE_MAPPER.isOneOf(
                properties.getType(), ImmutableList.of(AccountTypes.LOAN, AccountTypes.MORTGAGE));
    }

    // Currently logging loan details.
    @JsonIgnore
    public LoanAccount toTinkLoan() {
        return LoanAccount.builder(id, getBalance(accountBalance.getAccountingBalance()))
                .setAccountNumber(id)
                .setName(displayName)
                .setHolderName(new HolderName(owner.getName()))
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance(double balance) {
        String currency = properties.getCurrencyCode();

        if (Strings.isNullOrEmpty(currency)) {
            log.warn("Sparebanken Sor: No currency for account found. Defaulting to NOK.");

            return ExactCurrencyAmount.inNOK(balance);
        }

        return ExactCurrencyAmount.of(balance, currency);
    }
}
