package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String cashAccountType;
    private String ownerName;
    private String name;

    @JsonProperty("_links")
    private LinkEntity links;

    public Optional<TransactionalAccount> toTinkAccount(SebBalticsApiClient apiClient) {
        return (cashAccountType.toLowerCase().contains(AccountTypes.CURRENT))
                ? parseAccount(TransactionalAccountType.CHECKING, apiClient)
                : parseAccount(TransactionalAccountType.SAVINGS, apiClient);
    }

    private Optional<TransactionalAccount> parseAccount(
            TransactionalAccountType accountType, SebBalticsApiClient apiClient) {
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAvailableBalance(apiClient, resourceId)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(ownerName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .putPayload(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance(
            SebBalticsApiClient apiClient, String accountId) {

        BalanceResponse balanceResponse = apiClient.fetchAccountBalances(accountId);

        return Optional.ofNullable(balanceResponse.getBalances()).orElse(Collections.emptyList())
                .stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("Could not get balance"));
    }
}
