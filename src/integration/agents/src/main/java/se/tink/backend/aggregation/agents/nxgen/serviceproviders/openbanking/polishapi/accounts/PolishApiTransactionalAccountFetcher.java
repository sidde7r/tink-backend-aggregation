package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.AccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@RequiredArgsConstructor
public class PolishApiTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final PolishApiAccountClient apiClient;
    private final PolishApiPersistentStorage polishPersistentStorage;
    private final AccountTypeMapper accountTypeMapper;
    private final boolean shouldGetAccountNumbersFromToken;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        if (shouldGetAccountNumbersFromToken) {
            return fetchAccountDetailsFromListOfAccountNumbers()
                    .map(this::mapToTinkAccount)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else {
            return fetchAccountDetailsFromAccounts(apiClient.fetchAccounts())
                    .map(this::mapToTinkAccount)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }

    private Stream<AccountDetailsResponse> fetchAccountDetailsFromListOfAccountNumbers() {
        return polishPersistentStorage.getAccountIdentifiers().stream()
                .map(this::fetchAccountDetails)
                .filter(
                        response ->
                                PolishApiConstants.Accounts.isIndividualAccount(
                                        response.getAccount().getAccountHolderType()))
                .filter(
                        response ->
                                filterCheckingAndSavingsAccount(
                                        response.getAccount().getAccountType(),
                                        response.getAccount().getAccountTypeName()));
    }

    private Stream<AccountDetailsResponse> fetchAccountDetailsFromAccounts(
            AccountsResponse accountsResponse) {
        return accountsResponse.getAccounts().stream()
                .filter(
                        accountsEntity ->
                                filterCheckingAndSavingsAccount(
                                        accountsEntity.getAccountType(),
                                        accountsEntity.getAccountTypeName()))
                .map(accountsEntity -> fetchAccountDetails(accountsEntity.getAccountNumber()))
                .filter(
                        response ->
                                PolishApiConstants.Accounts.isIndividualAccount(
                                        response.getAccount().getAccountHolderType()));
    }

    private boolean filterCheckingAndSavingsAccount(
            AccountTypeEntity accountType, String accountTypeName) {
        return ImmutableList.of(AccountTypes.CHECKING, AccountTypes.SAVINGS)
                .contains(
                        accountTypeMapper
                                .translateByPattern(
                                        getConcatOfPossibleFieldsIndicatingAccountType(
                                                accountType, accountTypeName))
                                .orElse(AccountTypes.CHECKING));
    }

    private String getConcatOfPossibleFieldsIndicatingAccountType(
            AccountTypeEntity accountType, String accountTypeName) {
        return accountType.getCode() + accountType.getDescription() + accountTypeName;
    }

    private AccountDetailsResponse fetchAccountDetails(String accountIdentifier) {
        AccountDetailsResponse accountDetailsResponse =
                apiClient.fetchAccountDetails(accountIdentifier);
        accountDetailsResponse.getAccount().setApiAccountId(accountIdentifier);
        return accountDetailsResponse;
    }

    private Optional<TransactionalAccount> mapToTinkAccount(AccountDetailsResponse accountDetails) {
        return accountDetails.getAccount().toTinkAccount(accountTypeMapper);
    }
}
