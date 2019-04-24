package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

public class IngTransferDestinationFetcher implements TransferDestinationFetcher {
    private final IngApiClient apiClient;
    private final IngHelper ingHelper;

    public IngTransferDestinationFetcher(IngApiClient apiClient, IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.ingHelper = ingHelper;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return ingHelper
                .retrieveLoginResponse()
                .map(loginResponse -> getTransferDestinations(accounts, loginResponse))
                .orElseGet(TransferDestinationsResponse::new);
    }

    private TransferDestinationsResponse getTransferDestinations(
            Collection<Account> accounts, LoginResponseEntity loginResponse) {
        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();

        AccountListEntity accountListEntity =
                apiClient
                        .fetchAccounts(loginResponse)
                        .map(AccountsResponse::getAccounts)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                IngConstants.LogMessage
                                                        .TRANSFER_ACCOUNTS_NOT_FOUND));

        transferDestinations.addDestinations(
                getToOwnAccountsDestinations(accounts, accountListEntity));
        transferDestinations.addDestinations(
                getToAllAccountsDestinations(accounts, accountListEntity, loginResponse));

        return transferDestinations;
    }

    private Map<Account, List<TransferDestinationPattern>> getToOwnAccountsDestinations(
            Collection<Account> accounts, AccountListEntity accountListEntity) {

        List<GeneralAccountEntity> sourceAccounts =
                getSourceAccounts(
                        IngHelper.getAccountRulesPredicate(
                                IngConstants.AccountTypes.TRANSFER_TO_OWN_RULE),
                        accountListEntity);
        List<GeneralAccountEntity> destinationAccounts =
                getOwnDestinationAccounts(accountListEntity);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(accounts)
                .matchDestinationAccountsOn(
                        AccountIdentifier.Type.SEPA_EUR, SepaEurIdentifier.class)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getToAllAccountsDestinations(
            Collection<Account> accounts,
            AccountListEntity accountListEntity,
            LoginResponseEntity loginResponse) {
        List<GeneralAccountEntity> sourceAccounts =
                getSourceAccounts(
                        IngHelper.getAccountRulesPredicate(
                                IngConstants.AccountTypes.TRANSFER_TO_ALL_RULE),
                        accountListEntity);

        List<GeneralAccountEntity> destinationAccounts = getAllDestinationAccounts(loginResponse);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(accounts)
                .matchDestinationAccountsOn(
                        AccountIdentifier.Type.SEPA_EUR, SepaEurIdentifier.class)
                .addMultiMatchPattern(
                        AccountIdentifier.Type.SEPA_EUR, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> getSourceAccounts(
            Predicate<AccountEntity> accountEntityPredicate, AccountListEntity accounts) {
        return accounts.stream()
                .filter(accountEntityPredicate)
                .map(this::toGeneralAccountEntity)
                .collect(Collectors.toList());
    }

    private List<GeneralAccountEntity> getOwnDestinationAccounts(AccountListEntity accounts) {
        return accounts.stream().map(this::toGeneralAccountEntity).collect(Collectors.toList());
    }

    private List<GeneralAccountEntity> getAllDestinationAccounts(
            LoginResponseEntity loginResponse) {
        return apiClient
                .getBeneficiaries(loginResponse)
                .map(
                        trustedBeneficiariesResponse ->
                                trustedBeneficiariesResponse
                                        .getBeneficiaries()
                                        .getBeneficiaryList())
                .map(this::filterValidDestinationAccounts)
                .orElseGet(Collections::emptyList);
    }

    private List<GeneralAccountEntity> filterValidDestinationAccounts(
            List<BeneficiaryEntity> beneficiaryList) {
        return beneficiaryList.stream()
                .filter(
                        beneficiaryEntity ->
                                beneficiaryEntity.generalGetAccountIdentifier() != null)
                .collect(Collectors.toList());
    }

    private GeneralAccountEntity toGeneralAccountEntity(AccountEntity account) {
        AccountIdentifier accountIdentifier = new SepaEurIdentifier(account.getIbanNumber());

        return new TransferAccountEntity(accountIdentifier, account.getName());
    }
}
