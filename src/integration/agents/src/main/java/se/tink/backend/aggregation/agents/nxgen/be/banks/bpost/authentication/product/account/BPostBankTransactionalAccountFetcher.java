package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BPostBankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BPostBankApiClient apiClient;
    private final BPostBankAuthContext authContext;

    public BPostBankTransactionalAccountFetcher(
            BPostBankApiClient apiClient, BPostBankAuthContext authContext) {
        this.apiClient = apiClient;
        this.authContext = authContext;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            BPostBankAccountsResponseDTO response = apiClient.fetchAccounts(authContext);
            return mapToTransactionalAccounts(
                    response.savingsAccounts, TransactionalAccountType.SAVINGS);
        } catch (RequestException ex) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(ex.getMessage());
        }
    }

    private List<TransactionalAccount> mapToTransactionalAccounts(
            List<BPostBankAccountDTO> accountDTOs, TransactionalAccountType accountType) {
        return accountDTOs.stream()
                .map(a -> mapToTransactionalAccount(a, accountType))
                .collect(Collectors.toList());
    }

    private TransactionalAccount mapToTransactionalAccount(
            BPostBankAccountDTO accountDTO, TransactionalAccountType accountType) {
        final String iban = findIbanIdentifier(accountDTO);
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        accountDTO.bookedBalance, accountDTO.currency)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(accountDTO.alias)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                                .build())
                .build()
                .get();
    }

    private String findIbanIdentifier(BPostBankAccountDTO accountDTO) {
        final String ibanSchema = "IBAN";
        return accountDTO.accountIdentification.stream()
                .filter(id -> ibanSchema.equals(id.scheme))
                .findAny()
                .orElseThrow(
                        () ->
                                BankServiceError.BANK_SIDE_FAILURE.exception(
                                        "IBAN identifier not found"))
                .id;
    }
}
