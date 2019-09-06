package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountResourceEntity {
    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("accountId")
    private PsuAccountIdentificationEntity accountId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("linkedAccount")
    private String linkedAccount = null;

    @JsonProperty("cashAccountType")
    private CashAccountTypeEnumEntity cashAccountType = null;

    @JsonProperty("balances")
    private List<BalanceResourceEntity> balances = new ArrayList<BalanceResourceEntity>();

    @JsonProperty("psuStatus")
    private String psuStatus = null;

    @JsonProperty("_links")
    private AccountLinksEntity links = null;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId.getIban())
                                .withAccountNumber(accountId.getIban())
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN, accountId.getIban()))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(accountId.getIban())
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return balances.stream()
                .filter(item -> item.getBalanceType().equals(BalanceStatusEntity.XPCD))
                .findFirst()
                .map(
                        item ->
                                ExactCurrencyAmount.of(
                                        item.getBalanceAmount().getAmount(),
                                        item.getBalanceAmount().getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Balance not found"));
    }
}
