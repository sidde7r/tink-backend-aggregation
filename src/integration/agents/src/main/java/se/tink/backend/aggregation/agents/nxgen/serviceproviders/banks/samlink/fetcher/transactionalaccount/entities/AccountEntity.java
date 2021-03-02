package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class AccountEntity extends LinksResponse {
    private AccountIdEntity accountId;
    private String accountOwner;
    private AmountEntity usableBalance;
    private AmountEntity balance;
    private AmountEntity paymentLimit;
    private PermissionsEntity permissions;

    public AccountIdEntity getAccountId() {
        return accountId;
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    public AmountEntity getUsableBalance() {
        return usableBalance;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public AmountEntity getPaymentLimit() {
        return paymentLimit;
    }

    public PermissionsEntity getPermissions() {
        return permissions;
    }

    public String constructUniqueIdentifier() {
        return accountId.getIban();
    }

    public Optional<TransactionalAccount> toTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(usableBalance.toTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId.getIban())
                                .withAccountNumber(accountId.getIban())
                                .withAccountName(accountId.getIban())
                                .addIdentifier(new IbanIdentifier(accountId.getIban()))
                                .build())
                .setApiIdentifier(getLinks().getLinkPath(SamlinkConstants.LinkRel.TRANSACTIONS))
                .build();
    }
}
