package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountsEntity {

    private String resourceId;
    private String product;
    private String iban;
    private String name;
    private String currency;
    private String customerBic;

    public String getResourceId() {
        return resourceId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionalAccount toTinkAccount(List<BalanceEntity> balances) {

        return toCheckingAccount(balances);

        /*
            TODO: In the future when we can get the account type we will check it and
            return an object with correct TransactionalAccount type by using the code
            below.
        */

        /*Optional<AccountTypes> accountType =
                VolksbankConstants.ACCOUNT_TYPE_MAPPER.translate("Current");

        if (accountType.isPresent() && accountType.get() == AccountTypes.CHECKING)
            return toCheckingAccount(balances);
        else
            return null;*/
    }

    private TransactionalAccount toCheckingAccount(List<BalanceEntity> balances) {

        balances.sort((o1, o2) -> o2.getLastChangeDateTime().compareTo(o1.getLastChangeDateTime()));

        BalanceEntity lastBalance =
                balances.stream().findFirst().orElseThrow(IllegalStateException::new);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(VolksbankUtils.getAccountNumber(iban))
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .withBalance(BalanceModule.of(lastBalance.toAmount()))
                .setApiIdentifier(getResourceId())
                .build();
    }
}
