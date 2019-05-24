package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String bban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String name;
    private String resourceId;
    private String usage;
    private List<BalanceEntity> balances;

    public TransactionalAccount toTinkAccount(GetBalancesResponse getBalancesResponse) {
        balances = getBalancesResponse.getBalances();

        AccountTypes type =
                SkandiaConstants.ACCOUNT_TYPE_MAPPER
                        .translate(cashAccountType)
                        .orElse(AccountTypes.OTHER);

        switch (type) {
            case CHECKING:
                return toCheckingAccount();
            case OTHER:
            default:
                throw new IllegalStateException(ErrorMessages.INVALID_ACCOUNT_TYPE);
        }
    }

    private TransactionalAccount toCheckingAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getUniqueId())
                .setAccountNumber(bban)
                .setBalance(getAvailableBalance())
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElse(BalanceEntity.Default);
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getUniqueId() {
        String clearingNumber = iban.substring(4, 8);

        return clearingNumber + bban + "-" + clearingNumber + bban;
    }
}
