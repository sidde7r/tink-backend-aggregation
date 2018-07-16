package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

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

    public TransactionalAccount toTransactionalAccount() {
        return CheckingAccount.builder(accountId.getIban(), usableBalance.toTinkAmount())
                .setAccountNumber(accountId.getIban())
                .setBankIdentifier(getLinks().getLinkPath(SamlinkConstants.LinkRel.TRANSACTIONS))
                .build();
    }
}
