package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Accounts.CORPORATION;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.AccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

/**
 * Credit Cards are fetched through the same endpoints as CHECKING and SAVINGS accounts.
 *
 * <p>API does not return information about credit card number (it returns iban) and available
 * credit, hence as number IBAN is passed and available credit is set to 0.
 */
@RequiredArgsConstructor
public class PolishApiCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final PolishApiAccountClient apiClient;
    private final PolishApiPersistentStorage polishPersistentStorage;
    private final AccountTypeMapper accountTypeMapper;
    private final boolean shouldGetAccountIdentifiersFromToken;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        if (shouldGetAccountIdentifiersFromToken) {
            return fetchAccountDetailsFromListOfAccountIdentifiers()
                    .map(this::mapToTinkCreditCardAccount)
                    .collect(Collectors.toList());
        } else {
            return fetchAccountDetailsFromAccounts(apiClient.fetchAccounts())
                    .map(this::mapToTinkCreditCardAccount)
                    .collect(Collectors.toList());
        }
    }

    private Stream<AccountDetailsResponse> fetchAccountDetailsFromListOfAccountIdentifiers() {
        return polishPersistentStorage.getAccountIdentifiers().stream()
                .map(this::fetchAccountDetails)
                .filter(response -> filterCreditCards(response.getAccount().getAccountType()));
    }

    private Stream<AccountDetailsResponse> fetchAccountDetailsFromAccounts(
            AccountsResponse accountsResponse) {
        return accountsResponse.getAccounts().stream()
                .filter(accountsEntity -> filterCreditCards(accountsEntity.getAccountType()))
                .map(accountsEntity -> fetchAccountDetails(accountsEntity.getAccountNumber()))
                .filter(
                        response ->
                                filterIndividualAccounts(
                                        response.getAccount().getAccountHolderType()));
    }

    private boolean filterCreditCards(AccountTypeEntity accountType) {
        return AccountTypes.CREDIT_CARD
                == accountTypeMapper
                        .translateByPattern(getConcatOfDescriptionAndCode(accountType))
                        .orElse(AccountTypes.CHECKING);
    }

    private boolean filterIndividualAccounts(String accountHolderType) {
        return !CORPORATION.equals(accountHolderType);
    }

    private String getConcatOfDescriptionAndCode(AccountTypeEntity accountType) {
        return accountType.getCode() + accountType.getDescription();
    }

    private AccountDetailsResponse fetchAccountDetails(String accountIdentifier) {
        AccountDetailsResponse accountDetailsResponse =
                apiClient.fetchAccountDetails(accountIdentifier);
        accountDetailsResponse.getAccount().setApiAccountId(accountIdentifier);
        return accountDetailsResponse;
    }

    private CreditCardAccount mapToTinkCreditCardAccount(AccountDetailsResponse accountDetails) {
        return accountDetails.getAccount().toTinkCreditCardAccount();
    }
}
