package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer;

import static org.mockito.Mockito.mock;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankDefaultBankTransferExecutorTest {

    private SwedbankDefaultBankTransferExecutor swedbankDefaultBankTransferExecutor;

    @Before
    public void setUp() {
        Catalog catalog = mock(Catalog.class);
        SwedbankDefaultApiClient apiClient = mock(SwedbankDefaultApiClient.class);
        SwedbankTransferHelper transferHelper = mock(SwedbankTransferHelper.class);
        SwedbankStorage swedbankStorage = mock(SwedbankStorage.class);
        SwedbankDateUtils dateUtils = mock(SwedbankDateUtils.class);
        swedbankDefaultBankTransferExecutor =
                new SwedbankDefaultBankTransferExecutor(
                        catalog, apiClient, transferHelper, swedbankStorage, dateUtils);
    }

    @Test
    public void shouldThrowInvalidMsgExceptionIfRemittanceInfoIsNotValid() {
        Transfer transfer =
                SerializationUtils.deserializeFromString(getTransaction("<3"), Transfer.class);

        Throwable throwable =
                ThrowableAssert.catchThrowable(
                        () -> swedbankDefaultBankTransferExecutor.executeTransfer(transfer));

        Assertions.assertThat(throwable).isExactlyInstanceOf(TransferExecutionException.class);
        Assert.assertEquals(
                "Destination message can contain digits, letters in Swedish alphabet, or spaces.",
                throwable.getMessage());
    }

    @Test
    public void shouldThrowSourceAccountExceptionIfIdNotFound() {
        Transfer transfer =
                SerializationUtils.deserializeFromString(getTransaction(""), Transfer.class);

        Throwable throwable =
                ThrowableAssert.catchThrowable(
                        () -> swedbankDefaultBankTransferExecutor.executeTransfer(transfer));

        Assertions.assertThat(throwable).isExactlyInstanceOf(TransferExecutionException.class);
        Assert.assertEquals("Source account could not be found at bank.", throwable.getMessage());
    }

    private String getTransaction(String characters) {
        return "{"
                + "\"id\":\"b900555d-0312-4056-b549-30e1c53c9cac\", "
                + " \"amount\": 1.00, "
                + "\"credentialsId\": \"00000000-0000-0000-0000-000000000000\", "
                + "\"currency\": \"SEK\", "
                + "\"destinationUri\": \"sort-code://04000469430924?name=Ritesh%20Tink\", "
                + "\"sourceUri\": \"source-url://fakeUrl\", "
                + "\"sourceMessage\": \"Swedbank\", "
                + "\"userId\": \"00000000-0000-0000-0000-000000000000\", "
                + "\"type\": \"PAYMENT\", "
                + "\"dueDate\": 695260800, "
                + "\"messageType\": \"\", "
                + "\"payloadSerialized\": \"\", "
                + "\"remittanceInformation\": "
                + "{\"value\": \"Esbjorn"
                + characters
                + "\" }"
                + "}";
    }
}
