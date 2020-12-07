package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsItemEntity {

    private String cashAccountType;

    private String resourceId;

    @JsonProperty("_links")
    private Href linksEntity;

    private String usage;

    private String psuStatus;

    private String name;

    private String bicFi;

    private String currency;

    private AccountIdentificationEntity accountId;

    public String getCashAccountType() {
        return cashAccountType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Href getLinksEntity() {
        return linksEntity;
    }

    public String getUsage() {
        return usage;
    }

    public String getPsuStatus() {
        return psuStatus;
    }

    public String getName() {
        return name;
    }

    public String getBicFi() {
        return bicFi;
    }

    public String getCurrency() {
        return currency;
    }

    public Optional<TransactionalAccount> toTinkAccount(BalanceResponse balanceResponse) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        BnpParibasBaseConstants.ACCOUNT_TYPE_MAPPER,
                        cashAccountType,
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getAvailableBalance(balanceResponse)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId.getIban())
                                .withAccountNumber(accountId.getIban())
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(bicFi, accountId.getIban()))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance(BalanceResponse balanceResponse) {
        List<BalancesItemEntity> balancesList =
                Optional.ofNullable(balanceResponse.getBalances()).orElse(Collections.emptyList());

        Optional<BalancesItemEntity> futureBalance =
                balancesList.stream().filter(BalancesItemEntity::isFutureBalance).findFirst();

        Optional<BalancesItemEntity> closingBalance =
                balancesList.stream().filter(BalancesItemEntity::isClosingBalance).findFirst();

        BalancesItemEntity availableBalance;
        if (futureBalance.isPresent()) {
            availableBalance = futureBalance.get();
        } else if (closingBalance.isPresent()) {
            availableBalance = closingBalance.get();
        } else {
            throw new IllegalStateException();
        }

        return availableBalance.getAmountEntity().toAmount();
    }
}
