package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Status;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenSEBankIdSignerTest {

    private HandelsbankenSEBankIdSigner handelsbankenSEBankIdSigner;
    private DecoupledResponse decoupledResponse;

    @Before
    public void setUp() {
        HandelsbankenBaseApiClient apiClient;
        PersistentStorage persistentStorage;
        Credentials credentials;
        persistentStorage = mock(PersistentStorage.class);
        credentials = mock(Credentials.class);
        apiClient = mock(HandelsbankenBaseApiClient.class);
        decoupledResponse = mock(DecoupledResponse.class);
        handelsbankenSEBankIdSigner =
                new HandelsbankenSEBankIdSigner(persistentStorage, apiClient, credentials);
    }

    @Test
    public void shouldGetTimeoutWhenErrorIntentExpired() {
        when(decoupledResponse.getError()).thenReturn(Errors.INTENT_EXPIRED);

        BankIdStatus bankIdStatus =
                handelsbankenSEBankIdSigner.getBankIdStatusForDecoupledWithError(decoupledResponse);
        Assert.assertEquals(BankIdStatus.TIMEOUT, bankIdStatus);
    }

    @Test
    public void shouldGetFailedUnknownWhenRandomUnhandledError() {
        when(decoupledResponse.getError()).thenReturn(Errors.TOKEN_NOT_ACTIVE);

        BankIdStatus bankIdStatus =
                handelsbankenSEBankIdSigner.getBankIdStatusForDecoupledWithError(decoupledResponse);
        Assert.assertEquals(BankIdStatus.FAILED_UNKNOWN, bankIdStatus);
    }

    @Test
    public void shouldGetBankIdErrorWhenBankIDNotActivated() {
        when(decoupledResponse.getError()).thenReturn(Errors.BANKID_NOT_SHB_ACTIVATED);

        Throwable throwable =
                catchThrowable(
                        () ->
                                handelsbankenSEBankIdSigner.getBankIdStatusForDecoupledWithError(
                                        decoupledResponse));

        assertThat(throwable)
                .isExactlyInstanceOf(BankIdException.class)
                .hasMessage("Cause: BankIdError.AUTHORIZATION_REQUIRED");
    }

    @Test
    public void shouldGetBankIdErrorWhenBankIDNotApproved() {
        when(decoupledResponse.getError()).thenReturn(Errors.NOT_SHB_APPROVED);

        Throwable throwable =
                catchThrowable(
                        () ->
                                handelsbankenSEBankIdSigner.getBankIdStatusForDecoupledWithError(
                                        decoupledResponse));

        assertThat(throwable)
                .isExactlyInstanceOf(BankIdException.class)
                .hasMessage("Cause: BankIdError.AUTHORIZATION_REQUIRED");
    }

    @Test
    public void shouldGetStatusWaitingWhenInProgress() {
        when(decoupledResponse.getResult()).thenReturn(Status.IN_PROGRESS);

        BankIdStatus bankIdStatus =
                handelsbankenSEBankIdSigner.getBankIdStatusForDecoupledWithoutError(
                        decoupledResponse);
        Assert.assertEquals(BankIdStatus.WAITING, bankIdStatus);
    }

    @Test
    public void shouldGetStatusCancelledWhenUserCancelled() {
        when(decoupledResponse.getResult()).thenReturn(Status.USER_CANCEL);

        BankIdStatus bankIdStatus =
                handelsbankenSEBankIdSigner.getBankIdStatusForDecoupledWithoutError(
                        decoupledResponse);
        Assert.assertEquals(BankIdStatus.CANCELLED, bankIdStatus);
    }

    @Test
    public void shouldGetStatusDoneWhenComplete() {
        when(decoupledResponse.getResult()).thenReturn(Status.COMPLETE);

        BankIdStatus bankIdStatus =
                handelsbankenSEBankIdSigner.getBankIdStatusForDecoupledWithoutError(
                        decoupledResponse);
        Assert.assertEquals(BankIdStatus.DONE, bankIdStatus);
    }
}
