package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    @JsonProperty("_links")
    private LinksEntity links;

    private String currency;
    private String href;
    private String bban;
    private String iban;
    private String resourceId;
    private String name;
    private String product;

    public String getIban() {
        return iban;
    }

    public String getResourceId() {
        return resourceId;
    }

    public TransactionalAccount toTinkAccount(GetBalancesResponse getBalancesResponse) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getAccountNumber())
                .setAccountNumber(iban)
                .setBalance(getBalance(getBalancesResponse.getBalances()))
                .setAlias(getName())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .min(Comparator.comparing(BalanceEntity::getBalanceMappingPriority))
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.BALANCE_NOT_FOUND));
    }

    private String getName() {
        return Optional.ofNullable(name).orElse(iban);
    }

    private String getAccountNumber() {
        return Optional.ofNullable(bban).orElse(iban);
    }
}
