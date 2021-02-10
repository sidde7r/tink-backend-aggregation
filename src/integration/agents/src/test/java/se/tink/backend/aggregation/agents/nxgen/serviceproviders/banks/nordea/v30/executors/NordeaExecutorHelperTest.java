package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.NordeaBankIdStatus.BANKID_AUTOSTART_CANCELLED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.NordeaBankIdStatus.BANKID_AUTOSTART_COMPLETED;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.BankIdAutostartResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.CompleteTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.CompleteTransferResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public class NordeaExecutorHelperTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();
    SupplementalInformationController supplementalInformationController =
            mock(SupplementalInformationController.class);
    Catalog catalog = mock(Catalog.class);
    NordeaBaseApiClient nordeaBaseApiClient = mock(NordeaBaseApiClient.class);

    @Test
    public void pollWithBankidCompletedShouldResultInCompleteTransferResponse() {

        NordeaExecutorHelper nordeaExecutorHelper =
                new NordeaExecutorHelper(
                        supplementalInformationController,
                        catalog,
                        nordeaBaseApiClient,
                        new NordeaSEConfiguration(),
                        new RandomValueGeneratorImpl());

        CompleteTransferResponse completeTransferResponse =
                CompleteTransferResponse.builder().build();
        BankIdAutostartResponse bankIdAutostartResponse =
                BankIdAutostartResponse.builder()
                        .sessionId("sessionId")
                        .code("code")
                        .autoStartToken("autoStartToken")
                        .status(BANKID_AUTOSTART_COMPLETED)
                        .verifyAfter(2000)
                        .build();
        when(nordeaBaseApiClient.pollBankIdAutostart(Mockito.anyString()))
                .thenReturn(bankIdAutostartResponse);
        when(nordeaBaseApiClient.completeTransfer(
                        Mockito.anyString(), Mockito.any(CompleteTransferRequest.class)))
                .thenReturn(completeTransferResponse);

        nordeaExecutorHelper.poll("orderRef", "signingOrderId");

        assertEquals(
                nordeaExecutorHelper.poll("orderRef", "signingOrderId"), completeTransferResponse);
    }

    @Test
    public void pollWithBankidCancelledShouldResultInCancelledTransfer() {
        expectedException.expect(TransferExecutionException.class);
        expectedException.expectMessage("You cancelled the BankID process. Please try again.");

        NordeaExecutorHelper nordeaExecutorHelper =
                new NordeaExecutorHelper(
                        supplementalInformationController,
                        catalog,
                        nordeaBaseApiClient,
                        new NordeaSEConfiguration(),
                        new RandomValueGeneratorImpl());

        BankIdAutostartResponse bankIdAutostartResponse =
                BankIdAutostartResponse.builder()
                        .sessionId("sessionId")
                        .code("code")
                        .autoStartToken("autoStartToken")
                        .status(BANKID_AUTOSTART_CANCELLED)
                        .verifyAfter(2000)
                        .build();
        when(nordeaBaseApiClient.pollBankIdAutostart(Mockito.anyString()))
                .thenReturn(bankIdAutostartResponse);

        nordeaExecutorHelper.poll("orderRef", "signingOrderId");
    }

    @Test
    public void pollWithBankIdUnknownStatusShouldResultInFailedTransfer() {
        expectedException.expect(TransferExecutionException.class);
        expectedException.expectMessage("Failed to sign transfer with BankID");

        NordeaExecutorHelper nordeaExecutorHelper =
                new NordeaExecutorHelper(
                        supplementalInformationController,
                        catalog,
                        nordeaBaseApiClient,
                        new NordeaSEConfiguration(),
                        new RandomValueGeneratorImpl());

        BankIdAutostartResponse bankIdAutostartResponse =
                BankIdAutostartResponse.builder()
                        .sessionId("sessionId")
                        .code("code")
                        .autoStartToken("autoStartToken")
                        .status("unknown status")
                        .verifyAfter(2000)
                        .build();
        when(nordeaBaseApiClient.pollBankIdAutostart(Mockito.anyString()))
                .thenReturn(bankIdAutostartResponse);

        nordeaExecutorHelper.poll("orderRef", "signingOrderId");
    }
}
