package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.AccountHolderType;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class StarlingTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final StarlingApiClient apiClient;
    private final Set<String> handledHolderTypes;

    public StarlingTransactionalAccountFetcher(
            StarlingApiClient apiClient, Set<String> handledHolderTypes) {
        this.apiClient = apiClient;
        this.handledHolderTypes = handledHolderTypes;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> accounts = new ArrayList<>();
        for (AccountEntity account : apiClient.fetchAccounts().getAccounts()) {
            String accountHolderType = apiClient.fetchAccountHolder().getAccountHolderType();
            if (!handledHolderTypes.contains(accountHolderType)) {
                continue;
            }
            constructAccount(account, accountHolderType).ifPresent(accounts::add);
        }
        return accounts;
    }

    private Collection<String> getAccountHolderNames(String accountHolderType) {

        switch (accountHolderType) {
            case AccountHolderType.INDIVIDUAL:
            case AccountHolderType.JOINT:
                return Collections.singleton(
                        apiClient.fetchAccountHolderName().getAccountHolderName());
            case AccountHolderType.BUSINESS:
                return Collections.singleton(
                        apiClient.fetchBusinessAccountHolder().getCompanyName());
            case AccountHolderType.SOLE_TRADER:
                Set<String> holders = new HashSet<>();
                holders.add(apiClient.fetchAccountHolderName().getAccountHolderName());
                holders.add(apiClient.fetchSoleTraderAccountHolder().getTradingAsName());
                return holders;
            case AccountHolderType.BANKING_AS_A_SERVICE:
                return Collections.emptySet();
            default:
                throw new IllegalArgumentException(
                        "Unexpected account holder type: " + accountHolderType);
        }
    }

    private Optional<TransactionalAccount> constructAccount(
            AccountEntity account, String accountHolderType) {

        List<Holder> holders =
                getAccountHolderNames(accountHolderType).stream()
                        .map(Holder::of)
                        .collect(Collectors.toList());
        String accountUid = account.getAccountUid();
        String defaultCategoryId = account.getDefaultCategory();

        AccountIdentifiersResponse identifiers = apiClient.fetchAccountIdentifiers(accountUid);
        AccountBalanceResponse balance = apiClient.fetchAccountBalance(accountUid);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(balance.getAmount().toExactCurrencyAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifiers.getIban())
                                .withAccountNumber(
                                        identifiers.getSortCodeAccountNumber().getIdentifier())
                                .withAccountName(identifiers.getAccountIdentifier())
                                .addIdentifier(identifiers.getSortCodeAccountNumber())
                                .addIdentifier(identifiers.getIbanIdentifier())
                                .build())
                .addHolders(holders)
                .putInTemporaryStorage(UrlParams.CATEGORY_UID, defaultCategoryId)
                .setApiIdentifier(accountUid)
                .build();
    }
}
