package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.CheckingBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.SavingsBuildStep;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    @JsonProperty private String resourceId;
    @JsonProperty private String iban;
    @JsonProperty private String bban;
    @JsonProperty private String msisdn;
    @JsonProperty private String currency;
    @JsonProperty private String name;
    @JsonProperty private String product;
    @JsonProperty private String cashAccountType;
    @JsonProperty private String status;
    @JsonProperty private String bic;
    @JsonProperty private String linkedAccounts;
    @JsonProperty private String accountType;
    @JsonProperty private String details;
    @JsonProperty private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        AccountTypes accountType =
                ACCOUNT_TYPE_MAPPER.translate(cashAccountType).orElse(AccountTypes.OTHER);
        if (cashAccountType == null) {
            // field is optional
            accountType = AccountTypes.CHECKING;
        }
        switch (accountType) {
            case CHECKING:
                return Optional.of(toCheckingAccount());
            case SAVINGS:
                return Optional.of(toSavingsAccount());
            default:
                return Optional.empty();
        }
    }

    @JsonIgnore
    private Amount getLatestBalance() {
        if (balances == null) {
            return new Amount(currency, 0.0);
        }
        return balances.stream()
                .max(Comparator.comparing(BalanceEntity::getReferenceDate))
                .map(BalanceEntity::getAmount)
                .orElse(new Amount(currency, 0.0));
    }

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(linkName));
    }

    @JsonIgnore
    private TransactionalAccount toCheckingAccount() {
        AccountIdentifier accountIdentifier =
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban);

        CheckingBuildStep builder =
                CheckingAccount.builder()
                        .setUniqueIdentifier(iban)
                        .setAccountNumber(iban)
                        .setBalance(getLatestBalance())
                        .setAlias(name)
                        .addAccountIdentifier(accountIdentifier)
                        .setApiIdentifier(resourceId);

        if (!Strings.isNullOrEmpty(product)) {
            builder = builder.setProductName(product);
        }

        if (RedsysConstants.AccountType.BUSINESS.equalsIgnoreCase(accountType)) {
            builder = builder.addAccountFlags(AccountFlag.BUSINESS);
        }

        return builder.build();
    }

    @JsonIgnore
    private TransactionalAccount toSavingsAccount() {
        AccountIdentifier accountIdentifier =
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban);

        SavingsBuildStep builder =
                SavingsAccount.builder()
                        .setUniqueIdentifier(iban)
                        .setAccountNumber(iban)
                        .setBalance(getLatestBalance())
                        .setAlias(name)
                        .addAccountIdentifier(accountIdentifier)
                        .setApiIdentifier(resourceId);

        if (!Strings.isNullOrEmpty(product)) {
            builder = builder.setProductName(product);
        }

        if (RedsysConstants.AccountType.BUSINESS.equalsIgnoreCase(accountType)) {
            builder = builder.addAccountFlags(AccountFlag.BUSINESS);
        }

        return builder.build();
    }
}
