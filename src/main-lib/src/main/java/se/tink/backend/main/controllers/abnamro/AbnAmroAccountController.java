package se.tink.backend.main.controllers.abnamro;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.core.Account;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateAccountRequest;

public class AbnAmroAccountController {

    private final boolean isAbnAmroCluster;
    private final SystemServiceFactory systemServiceFactory;

    @Inject
    public AbnAmroAccountController(Cluster cluster, SystemServiceFactory systemServiceFactory) {
        this.isAbnAmroCluster = Objects.equals(cluster, Cluster.ABNAMRO);
        this.systemServiceFactory = systemServiceFactory;
    }

    public void updateAccounts(Credentials credentials, List<Account> accounts) {

        Preconditions.checkState(isAbnAmroCluster);
        Preconditions.checkNotNull(accounts, "Accounts can not be null");
        Preconditions.checkNotNull(credentials, "Credentials can not be null");
        Preconditions.checkNotNull(credentials.getId(), "Credentials.Id can not be null");
        Preconditions.checkNotNull(credentials.getUserId(), "Credentials.UserId can not be null");

        for (Account account : accounts) {
            account.setUserId(credentials.getUserId());
            account.setCredentialsId(credentials.getId());

            if (Objects.equals(account.getType(), AccountTypes.CREDIT_CARD)) {
                account.setAccountNumber(AbnAmroUtils.maskCreditCardContractNumber(account.getAccountNumber()));
            }

            updateAccount(account);
        }
    }

    private void updateAccount(Account account) {

        UpdateAccountRequest updateAccountsRequest = new UpdateAccountRequest();

        updateAccountsRequest.setAccount(account);
        updateAccountsRequest.setAccountFeatures(AccountFeatures.createEmpty());
        updateAccountsRequest.setCredentialsId(account.getCredentialsId());
        updateAccountsRequest.setUser(account.getUserId());

        systemServiceFactory.getUpdateService().updateAccount(updateAccountsRequest);
    }
}
