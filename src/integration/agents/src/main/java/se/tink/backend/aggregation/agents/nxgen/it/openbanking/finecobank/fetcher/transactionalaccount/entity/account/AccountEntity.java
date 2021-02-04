package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.enums.AccountFlag;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String name;
    private List<BalanceEntity> balances;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        final String nameTrimmed = name.substring(0, name.length() - 4);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        FinecoBankConstants.ACCOUNT_TYPE_MAPPER,
                        nameTrimmed,
                        TransactionalAccountType.CHECKING)
                .withBalance(FinecoBalanceTransform.calculate(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(AccountIdentifier.create(Type.IBAN, iban))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(iban)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .build();
    }
}
