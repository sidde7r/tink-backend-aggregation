package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.AccountHolderType;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class StarlingTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final StarlingApiClient apiClient;

    public StarlingTransactionalAccountFetcher(StarlingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        String accountHolderName = getAccountHolderName();

        return apiClient.fetchAccounts().stream()
                .map(AccountEntity::getAccountUid)
                .map(uid -> constructAccount(uid, accountHolderName))
                .collect(Collectors.toList());
    }

    private String getAccountHolderName() {
        AccountHolderResponse accountHolder = apiClient.fetchAccountHolder();
        switch (accountHolder.getAccountHolderType()) {
            case AccountHolderType.INDIVIDUAL:
                return apiClient.fetchIndividualAccountHolder().getFullName();
            case AccountHolderType.JOINT:
                return apiClient.fetchJointAccountHolder().getCombinedFullName();
            default:
                throw new RuntimeException(
                        "Unexpected account holder type: " + accountHolder.getAccountHolderType());
        }
    }

    private TransactionalAccount constructAccount(
            final String accountUid, final String holderName) {

        AccountIdentifiersResponse identifiers = apiClient.fetchAccountIdentifiers(accountUid);
        AccountBalanceResponse balance = apiClient.fetchAccountBalance(accountUid);

        return CheckingAccount.builder()
                .setUniqueIdentifier(identifiers.getIban())
                .setAccountNumber(identifiers.getAccountIdentifier())
                .setBalance(balance.getAmount())
                .setAlias(identifiers.getAccountIdentifier())
                .addAccountIdentifier(identifiers.getIbanIdentifier())
                .addAccountIdentifier(identifiers.getSortCodeIdentifier())
                .setApiIdentifier(accountUid)
                .addHolderName(holderName)
                .build();
    }
}
