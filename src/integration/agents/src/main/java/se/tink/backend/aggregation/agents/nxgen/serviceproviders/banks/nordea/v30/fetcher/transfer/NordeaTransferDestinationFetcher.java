package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class NordeaTransferDestinationFetcher implements TransferDestinationFetcher {
    private final NordeaBaseApiClient apiClient;
    private List<AccountEntity> accountEntityList = Lists.newArrayList();

    public NordeaTransferDestinationFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        accountEntityList = apiClient.fetchAccount().getAccounts();
        return TransferDestinationsResponse.builder()
                .addTransferDestinations(getPaymentDestinations(accounts))
                .addTransferDestinations(getTransferDestinations(accounts))
                .build();
    }

    // payments
    private Map<Account, List<TransferDestinationPattern>> getPaymentDestinations(
            Collection<Account> accounts) {
        // all accounts that can pay from
        final List<GeneralAccountEntity> paymentSourceAccounts =
                accountEntityList.stream()
                        .filter(account -> Objects.nonNull(account.getPermissions()))
                        .filter(account -> account.getPermissions().isCanPayPgbgFromAccount())
                        .collect(Collectors.toList());

        // all account that can pay to
        final List<GeneralAccountEntity> paymentDestinationAccounts =
                apiClient.fetchBeneficiaries().getBeneficiaries().stream()
                        .filter(BeneficiariesEntity::isPgOrBg)
                        .collect(Collectors.toList());

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(paymentSourceAccounts)
                .setDestinationAccounts(paymentDestinationAccounts)
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifierType.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifierType.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }

    // bank transfers
    private Map<Account, List<TransferDestinationPattern>> getTransferDestinations(
            Collection<Account> accounts) {
        // all accounts that can transfer from
        final List<GeneralAccountEntity> sourceAccounts =
                accountEntityList.stream()
                        .filter(account -> Objects.nonNull(account.getPermissions()))
                        .filter(account -> account.getPermissions().isCanTransferFromAccount())
                        .collect(Collectors.toList());

        // all accounts that can transfer to
        final List<GeneralAccountEntity> destinationAccounts =
                Stream.concat(
                                accountEntityList.stream()
                                        .filter(
                                                account ->
                                                        Objects.nonNull(account.getPermissions()))
                                        .filter(
                                                account ->
                                                        account.getPermissions()
                                                                .isCanTransferToAccount()),
                                apiClient.fetchBeneficiaries().getBeneficiaries().stream()
                                        .filter(BeneficiariesEntity::isLBAN))
                        .collect(Collectors.toList());

        return new TransferDestinationPatternBuilder()
                .setTinkAccounts(accounts)
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .addMultiMatchPattern(AccountIdentifierType.SE, TransferDestinationPattern.ALL)
                .build();
    }
}
