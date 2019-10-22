package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import static se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType.CHECKING;
import static se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType.SAVINGS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.STORAGE;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Account;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.util.CurrencyMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private static final int TRANSACTIONAL_ACCOUNTS_INDEX = 2;
    private static TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(CHECKING, "MN")
                    .put(SAVINGS, "CR")
                    .build();

    private final SantanderApiClient apiClient;
    private final CurrencyMapper currencyMapper;

    public SantanderTransactionalAccountFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
        this.currencyMapper = new CurrencyMapper();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ApiResponse<List<Map<String, String>>> apiResponse = apiClient.fetchAccounts();
        return deserializeAccounts(apiResponse.getBusinessData());
    }

    private List<TransactionalAccount> deserializeAccounts(
            List<List<Map<String, String>>> accounts) {
        List<Map<String, String>> checkingAccounts = accounts.get(TRANSACTIONAL_ACCOUNTS_INDEX);

        return checkingAccounts.stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), Collections::unmodifiableList));
    }

    private Optional<TransactionalAccount> toTinkAccount(Map<String, String> obj) {
        String currencyCode =
                currencyMapper
                        .convertToCode(Integer.parseInt(obj.get(Account.CURRENCY_NUMERIC_CODE)))
                        .getCurrencyCode();

        return TransactionalAccount.nxBuilder()
                .withType(
                        ACCOUNT_TYPE_MAPPER
                                .translate(obj.get(Account.ACCOUNT_TYPE))
                                .orElseThrow(
                                        () -> new IllegalArgumentException("Unknown account type")))
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        obj.get(Account.AVAILABLE_BALANCE), currencyCode)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(obj.get(Account.IBAN))
                                .withAccountNumber(obj.get(Account.ACCOUNT_NUMBER))
                                .withAccountName(obj.get(Account.ACCOUNT_NAME))
                                .addIdentifier(
                                        new IbanIdentifier(
                                                obj.get(Account.BIC), obj.get(Account.IBAN)))
                                .setProductName(obj.get(Account.PRODUCT_NAME))
                                .build())
                .putInTemporaryStorage(STORAGE.CURRENCY_CODE, currencyCode)
                .putInTemporaryStorage(STORAGE.BRANCH_CODE, obj.get(Account.BRANCH_CODE))
                .build();
    }
}
