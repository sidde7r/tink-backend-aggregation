package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

import java.util.Collection;
import java.util.stream.Collectors;

public class StarlingTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final StarlingApiClient apiClient;

    public StarlingTransactionalAccountFetcher(
            StarlingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AccountHolderResponse accountHolder = apiClient.fetchAccountHolder();

        return apiClient.fetchAccounts().stream()
                .map(AccountEntity::getAccountUid)
                .map(uid -> constructAccount(uid, accountHolder.getFullName()))
                .collect(Collectors.toList());
    }

    private TransactionalAccount constructAccount(final String accountUid, final String holderName) {

//        AccountIdentifiersResponse identifiers = apiClient.fetchAccountIdentifiers(accountUid);
        AccountBalanceResponse balance = apiClient.fetchAccountBalance(accountUid);

        return CheckingAccount.builder()
//                .setUniqueIdentifier(identifiers.getIban())
                .setUniqueIdentifier(accountUid)
//                .setAccountNumber(identifiers.getIban())
                .setAccountNumber(accountUid)
                .setBalance(balance.getAmount())
//                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN,  identifiers.getIban()))
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.TINK, accountUid))
                .setApiIdentifier(accountUid)
                .addHolderName(holderName)
                .build();
    }
}
