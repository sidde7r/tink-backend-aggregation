package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.StarlingAccountHolderType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class StarlingTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final StarlingApiClient apiClient;

    public StarlingTransactionalAccountFetcher(StarlingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().stream()
                .map(
                        accountEntity ->
                                constructAccount(accountEntity, apiClient.fetchAccountHolderType()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> constructAccount(
            AccountEntity account, StarlingAccountHolderType accountHolderType) {

        List<Party> parties =
                getAccountPartiesNames(accountHolderType).stream()
                        .map(name -> new Party(name, Party.Role.HOLDER))
                        .collect(Collectors.toList());
        String accountUid = account.getAccountUid();
        String defaultCategoryId = account.getDefaultCategory();

        AccountIdentifiersResponse identifiers = apiClient.fetchAccountIdentifiers(accountUid);
        AccountBalanceResponse balance = apiClient.fetchAccountBalance(accountUid);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(AccountBalanceResponse.createBalanceModule(balance))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifiers.getIban())
                                .withAccountNumber(
                                        identifiers.getSortCodeAccountNumber().getIdentifier())
                                .withAccountName(account.getName())
                                .addIdentifier(identifiers.getSortCodeAccountNumber())
                                .addIdentifier(identifiers.getIbanIdentifier())
                                .build())
                .addParties(parties)
                .putInTemporaryStorage(UrlParams.CATEGORY_UID, defaultCategoryId)
                .putInTemporaryStorage(
                        StarlingConstants.ACCOUNT_CREATION_DATE_TIME, account.getCreatedAt())
                .setApiIdentifier(accountUid)
                .setHolderType(accountHolderType.toTinkAccountHolderType())
                .build();
    }

    private Collection<String> getAccountPartiesNames(StarlingAccountHolderType accountHolderType) {
        switch (accountHolderType) {
            case JOINT:
            case INDIVIDUAL:
                return Collections.singleton(
                        apiClient.fetchAccountHolderName().getAccountHolderName());
            case SOLE_TRADER:
                Set<String> holders = new HashSet<>();
                holders.add(apiClient.fetchAccountHolderName().getAccountHolderName());
                holders.add(apiClient.fetchSoleTraderAccountHolder().getTradingAsName());
                return holders;
            case BUSINESS:
                return Collections.singleton(
                        apiClient.fetchBusinessAccountHolder().getCompanyName());
            case UNKNOWN:
            case BANKING_AS_A_SERVICE:
                return Collections.emptyList();
            default:
                throw new IllegalArgumentException(
                        "Unexpected account holder type: " + accountHolderType);
        }
    }
}
