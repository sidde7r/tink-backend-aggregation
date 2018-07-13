package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.GeneralUtils;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.BeneficiaryDto;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BelgianIdentifier;

public class KbcTransferDestinationFetcher implements TransferDestinationFetcher {

    private final KbcApiClient apiClient;

    public KbcTransferDestinationFetcher(KbcApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        List<GeneralAccountEntity> sourceAccounts = getAllSourceAccounts();
        List<BeneficiaryDto> destinationAccounts = apiClient.beneficiariesHistory().getBeneficiaries();

        return new TransferDestinationsResponse(
                getTransferAccountDestinations(sourceAccounts, destinationAccounts, accounts));
    }

    private List<GeneralAccountEntity> getAllSourceAccounts() {
        List<AgreementDto> accountsForTransferToOwn = apiClient.accountsForTransferToOwn().getAgreements();
        List<AgreementDto> accountsForTransferToOther = apiClient.accountsForTransferToOther().getAgreements();

        return GeneralUtils.concat(accountsForTransferToOwn, accountsForTransferToOther);
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(
            List<GeneralAccountEntity> sourceAccounts, List<BeneficiaryDto> destinationAccounts,
            Collection<Account> tinkAccounts) {

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(tinkAccounts)
                .matchDestinationAccountsOn(AccountIdentifier.Type.BE, BelgianIdentifier.class)
                .addMultiMatchPattern(AccountIdentifier.Type.BE, TransferDestinationPattern.ALL)
                .build();
    }

}
