package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.credit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtCreditCardParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.amount.Amount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngAtCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(IngAtCreditCardAccountFetcher.class);
    private IngAtApiClient apiClient;
    private IngAtSessionStorage sessionStorage;

    public IngAtCreditCardAccountFetcher(
            final IngAtApiClient apiClient, final IngAtSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private static boolean isCreditCardAccount(AccountReferenceEntity r) {
        return IngAtConstants.ACCOUNT_TYPE_MAPPER.isCreditCardAccount(r.getType());
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final WebLoginResponse webLoginResponse =
                sessionStorage
                        .getWebLoginResponse()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find login response when fetching accounts"));
        final List<AccountReferenceEntity> accountReferences =
                webLoginResponse
                        .getAccountReferenceEntities()
                        .stream()
                        .filter(IngAtCreditCardAccountFetcher::isCreditCardAccount)
                        .collect(Collectors.toList());
        final Collection<CreditCardAccount> res = new ArrayList<>();
        for (AccountReferenceEntity accountReference : accountReferences) {
            final HttpResponse response =
                    apiClient.getAccountDetails(new URL(accountReference.getUrl()));
            toCreditCardAccount(response, webLoginResponse, accountReference).ifPresent(res::add);
        }

        return res;
    }

    private Optional<CreditCardAccount> toCreditCardAccount(
            HttpResponse response,
            WebLoginResponse webLoginResponse,
            AccountReferenceEntity accountReference) {
        final IngAtCreditCardParser parser =
                new IngAtCreditCardParser(response.getBody(String.class));
        final String accountHolder = webLoginResponse.getAccountHolder();
        final Amount availableCredit = parser.getAvailableCredit();
        final String bankIdentifier = parser.getIdentifier();
        final String connectedAccountNumber = parser.getConnectedAccountNumber();
        final Amount saldo = parser.getSaldo();
        final String uniqueIdentifier = parser.getUniqueIdentifier();
        return Optional.of(
                CreditCardAccount.builder(uniqueIdentifier, saldo, availableCredit)
                        .setHolderName(new HolderName(accountHolder))
                        .setBankIdentifier(bankIdentifier)
                        .setAccountNumber(connectedAccountNumber)
                        .setName(bankIdentifier)
                        .putInTemporaryStorage(
                                IngAtConstants.Storage.ACCOUNT_INDEX.name(),
                                accountReference.getAccountIndex())
                        .putInTemporaryStorage(
                                IngAtConstants.Storage.TRANSACTIONS.name(),
                                response.getBody(String.class))
                        .build());
    }
}
