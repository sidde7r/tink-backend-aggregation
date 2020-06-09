package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account;

import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private String cashAccountType;
    private String name;
    private String ownerName;

    public String getResourceId() {
        return resourceId;
    }

    public String getIban() {
        return iban;
    }

    public Optional<TransactionalAccount> toTinkAccount(
            AccountEntity accountDetailsEntity, ExactCurrencyAmount balanceAmount) {
        String finalIban = getFinalIban(accountDetailsEntity);
        String finalName = getFinalName(accountDetailsEntity);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balanceAmount))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(finalIban)
                                .withAccountNumber(finalIban)
                                .withAccountName(finalName)
                                .addIdentifier(new IbanIdentifier(finalIban))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .addHolderName(accountDetailsEntity.ownerName)
                .build();
    }

    private String getFinalIban(AccountEntity accountDetailsEntity) {
        return ObjectUtils.firstNonNull(iban, accountDetailsEntity.iban);
    }

    private String getFinalName(AccountEntity accountDetailsEntity) {
        return ObjectUtils.firstNonNull(name, accountDetailsEntity.name, "");
    }
}
