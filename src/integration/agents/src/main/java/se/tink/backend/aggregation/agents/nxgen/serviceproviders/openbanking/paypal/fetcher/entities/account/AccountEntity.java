package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.account;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

public class AccountEntity {
    private String id;
    private String email;

    public AccountEntity(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public TransactionalAccount toTinkAccount(AccountBalanceResponse accountBalanceResponse) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(email)
                                .withAccountNumber(email)
                                .withAccountName(email)
                                .addIdentifier(AccountIdentifier.create(Type.TINK, email))
                                .build())
                .withBalance(BalanceModule.of(accountBalanceResponse.getAvailableBalance()))
                .build();
    }
}
