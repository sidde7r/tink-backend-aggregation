package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String ownerName;
    private String product;
    private String status;
    private String bic;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    private List<BalanceEntity> balances;

    public Optional<TransactionalAccount> toTinkAccount() {
        if (!getBalance().isPresent()) {
            log.error("Balance fetch error: {}", BankdataConstants.LogTags.ERROR_FETCHING_BALANCE);

            return Optional.empty();
        }

        log.info("[Bankdata] Account type check: [{}][{}]", name, product);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance().get()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(Strings.isNullOrEmpty(name) ? product : name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(ownerName)
                .putInTemporaryStorage(BankdataConstants.StorageKeys.ACCOUNT_ID, iban)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .putInTemporaryStorage(
                        BankdataConstants.StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .build();
    }

    public String getTransactionLink() {
        return Optional.ofNullable(links).map(a -> a.getTransactions().getLink()).orElse("");
    }

    public String getBalancesLink() {
        return Optional.ofNullable(links).map(a -> a.getBalances().getLink()).orElse("");
    }

    private Optional<ExactCurrencyAmount> getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceEntity::toAmount);
    }

    public List<BalanceEntity> getBalances() {
        return balances;
    }

    public void setBalances(final List<BalanceEntity> balances) {
        this.balances = balances;
    }

    private boolean doesMatchWithAccountCurrency(final BalanceEntity balance) {
        return balance.isForwardAvailable() && balance.isInCurrency(currency);
    }
}
