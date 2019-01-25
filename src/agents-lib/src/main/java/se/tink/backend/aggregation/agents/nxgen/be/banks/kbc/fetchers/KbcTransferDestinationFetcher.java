package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers;

import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.BeneficiaryDto;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KbcTransferDestinationFetcher implements TransferDestinationFetcher {

    private final KbcApiClient apiClient;
    private String userLanguage;

    public KbcTransferDestinationFetcher(KbcApiClient apiClient, String userLanguage) {
        this.apiClient = apiClient;
        this.userLanguage = userLanguage;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> tinkAccounts) {
        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();
        transferDestinations.addDestinations(getToOwnAccountsDestinations(tinkAccounts));
        transferDestinations.addDestinations(getToOtherAccountsDestinations(tinkAccounts));
        return transferDestinations;
    }

    private Map<Account, List<TransferDestinationPattern>> getToOwnAccountsDestinations(
            Collection<Account> tinkAccounts) {

        List<AgreementDto> sourceAccounts = apiClient.accountsForTransferToOwn().getAgreements();
        List<AgreementDto> destinationAccounts = apiClient.fetchAccounts(userLanguage).getAgreements();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(tinkAccounts)
                .matchDestinationAccountsOn(AccountIdentifier.Type.SEPA_EUR, SepaEurIdentifier.class)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getToOtherAccountsDestinations(
            Collection<Account> tinkAccounts) {

        List<AgreementDto> sourceAccounts = apiClient.accountsForTransferToOther().getAgreements();
        List<BeneficiaryDto> destinationAccounts = apiClient.beneficiariesHistory().getBeneficiaries();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(tinkAccounts)
                .matchDestinationAccountsOn(AccountIdentifier.Type.SEPA_EUR, SepaEurIdentifier.class)
                .addMultiMatchPattern(AccountIdentifier.Type.SEPA_EUR, TransferDestinationPattern.ALL)
                .build();
    }

}
