package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.BeneficiaryDto;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

public class KbcTransferDestinationFetcher implements TransferDestinationFetcher {

    private final KbcApiClient apiClient;
    private String userLanguage;
    private final SessionStorage sessionStorage;

    public KbcTransferDestinationFetcher(
            KbcApiClient apiClient, String userLanguage, final SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.userLanguage = userLanguage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(
            Collection<Account> tinkAccounts) {
        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();
        transferDestinations.addDestinations(getToOwnAccountsDestinations(tinkAccounts, cipherKey));
        transferDestinations.addDestinations(
                getToOtherAccountsDestinations(tinkAccounts, cipherKey));
        return transferDestinations;
    }

    private Map<Account, List<TransferDestinationPattern>> getToOwnAccountsDestinations(
            Collection<Account> tinkAccounts, final byte[] cipherKey) {
        List<AgreementDto> sourceAccounts =
                apiClient.accountsForTransferToOwn(cipherKey).getAgreements();
        List<AgreementDto> destinationAccounts =
                apiClient.fetchAccounts(userLanguage, cipherKey).getAgreements();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(tinkAccounts)
                .matchDestinationAccountsOn(
                        AccountIdentifier.Type.SEPA_EUR, SepaEurIdentifier.class)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getToOtherAccountsDestinations(
            Collection<Account> tinkAccounts, final byte[] cipherKey) {
        List<AgreementDto> sourceAccounts =
                apiClient.accountsForTransferToOther(cipherKey).getAgreements();
        List<BeneficiaryDto> destinationAccounts =
                apiClient.beneficiariesHistory(cipherKey).getBeneficiaries();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(tinkAccounts)
                .matchDestinationAccountsOn(
                        AccountIdentifier.Type.SEPA_EUR, SepaEurIdentifier.class)
                .addMultiMatchPattern(
                        AccountIdentifier.Type.SEPA_EUR, TransferDestinationPattern.ALL)
                .build();
    }
}
