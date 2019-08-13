package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Optional;
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
    private HrefEntity linksEntity;

    private String usage;

    private String psuStatus;

    private String name;

    private String bicFi;

    private String currency;

    public String getCashAccountType() {
        return cashAccountType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public HrefEntity getLinksEntity() {
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

    private TransactionalAccountType getAccountType() {
        return BnpParibasBaseConstants.ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .orElse(TransactionalAccountType.OTHER);
    }

    public TransactionalAccount toTinkAccount(BalanceResponse balanceResponse) {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withBalance(BalanceModule.of(getAvailableBalance(balanceResponse)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(resourceId)
                                .withAccountNumber(resourceId)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(resourceId))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(BnpParibasBaseConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance(BalanceResponse balanceResponse) {

        return Optional.ofNullable(balanceResponse.getBalances()).orElse(Collections.emptyList())
                .stream()
                .filter(BalancesItemEntity::isAvailableBalance)
                .findFirst()
                .map(BalancesItemEntity::getAmountEntity)
                .map(AmountEntity::toAmount)
                .orElseThrow(IllegalStateException::new);
    }
}
