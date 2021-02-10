package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.transfer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelperImpl;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankTransferHelperTest {
    private SwedbankTransferHelper transferHelper;
    @Rule public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        SupplementalInformationController supplementalInformationController =
                Mockito.mock(SupplementalInformationController.class);
        SupplementalInformationHelper supplementalInformationHelper =
                Mockito.mock(SupplementalInformationHelperImpl.class);
        Catalog catalog = Mockito.mock(Catalog.class);
        SwedbankDefaultApiClient apiClient = Mockito.mock(SwedbankDefaultApiClient.class);
        boolean isBankId = true;

        transferHelper =
                new SwedbankTransferHelper(
                        supplementalInformationController,
                        catalog,
                        supplementalInformationHelper,
                        apiClient,
                        isBankId);
    }

    @Test
    public void verify_ConfirmedTransfer_Succeeds() {
        ConfirmTransferResponse confirmTransferResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTransferConfirmationData.TRANSFER_CONFIRMED_RESPONSE,
                        ConfirmTransferResponse.class);

        transferHelper.confirmSuccessfulTransferOrThrow(
                confirmTransferResponse, SwedbankTransferConfirmationData.TRANSFER_ID);
    }

    @Test
    public void verify_RejectedTransactionWithKnownCause_ThrowsWithSpecificMessage() {
        thrown.expect(TransferExecutionException.class);
        thrown.expectMessage(
                "The transfer amount is larger than what is available on the account.");

        ConfirmTransferResponse confirmTransferResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTransferConfirmationData
                                .TRANSFER_REJECTED_DUE_TO_INSUFFICIENT_FUNDS_RESPONSE,
                        ConfirmTransferResponse.class);

        transferHelper.confirmSuccessfulTransferOrThrow(
                confirmTransferResponse, SwedbankTransferConfirmationData.TRANSFER_ID);
    }

    @Test
    public void verify_RejectedTransactionWithUnknownCause_ThrowsWithGenericMessage() {
        thrown.expect(TransferExecutionException.class);
        thrown.expectMessage("Transfer rejected by the Bank");

        ConfirmTransferResponse confirmTransferResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTransferConfirmationData
                                .TRANSFER_REJECTED_WITH_UNKNOWN_CAUSE_RESPONSE,
                        ConfirmTransferResponse.class);

        transferHelper.confirmSuccessfulTransferOrThrow(
                confirmTransferResponse, SwedbankTransferConfirmationData.TRANSFER_ID);
    }

    @Test
    public void verify_RejectedTransactionWithNoCause_ThrowsWithGenericMessage() {
        thrown.expect(TransferExecutionException.class);
        thrown.expectMessage("Transfer rejected by the Bank");

        ConfirmTransferResponse confirmTransferResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTransferConfirmationData.TRANSFER_REJECTED_WITH_NO_CAUSE_RESPONSE,
                        ConfirmTransferResponse.class);

        transferHelper.confirmSuccessfulTransferOrThrow(
                confirmTransferResponse, SwedbankTransferConfirmationData.TRANSFER_ID);
    }

    @Test
    public void verify_RejectedTransactionWithMultipleCauses_ThrowsWithGenericMessage() {
        thrown.expect(TransferExecutionException.class);
        thrown.expectMessage("Transfer rejected by the Bank");

        ConfirmTransferResponse confirmTransferResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTransferConfirmationData
                                .TRANSFER_REJECTED_WITH_MULTIPLE_CAUSES_RESPONSE,
                        ConfirmTransferResponse.class);

        transferHelper.confirmSuccessfulTransferOrThrow(
                confirmTransferResponse, SwedbankTransferConfirmationData.TRANSFER_ID);
    }

    @Test
    public void verify_NoConfirmedOrRejectedTransfers_ThrowsWithFailureToConfirmMessage() {
        thrown.expect(TransferExecutionException.class);
        thrown.expectMessage("An error occurred when confirming the transfer");

        ConfirmTransferResponse confirmTransferResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTransferConfirmationData
                                .NO_CONFIRMED_OR_REJECTED_TRANSFERS_RESPONSE,
                        ConfirmTransferResponse.class);

        transferHelper.confirmSuccessfulTransferOrThrow(
                confirmTransferResponse, SwedbankTransferConfirmationData.TRANSFER_ID);
    }
}
