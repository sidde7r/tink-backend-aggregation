package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor;

import java.util.Collections;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient.Creatable;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEPaymentAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSpecificationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSpecificationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.HandelsbankenSEBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter.Messages;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.backend.aggregation.mocks.ResultCaptor;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Careful with this test! Actual transfers made!
 */
public class HandelsbankenSEBankTransferExecutorTest {



    private String destinationAccountNumber;
    private ResultCaptor<TransferSpecificationResponse> specificationResponseCaptor;

    @Test
    public void canTransferToUnknown() throws Exception{
        destinationAccountNumber = HandelsbankenSETestConfig.UNKNOWN_ACCOUNT;

        doTransfer();

        assertHandelsbankenHasConfirmedInformationForSigning();
    }

    @Test
    public void canTransferToKnown() throws Exception{
        destinationAccountNumber = HandelsbankenSETestConfig.KNOWN_ACCOUNT;

        doTransfer();

        assertHandelsbankenHasConfirmedInformationForSigning();
    }

    private void doTransfer() throws se.tink.backend.aggregation.agents.exceptions.AuthenticationException,
            se.tink.backend.aggregation.agents.exceptions.AuthorizationException {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setField(Field.Key.USERNAME, HandelsbankenSETestConfig.HEDBERG);

        HandelsbankenSEConfiguration configuration = new HandelsbankenSEConfiguration();
        HandelsbankenPersistentStorage persistentStorage = new HandelsbankenPersistentStorage(new PersistentStorage(),
                Collections.emptyMap());
        HandelsbankenSessionStorage sessionStorage = new HandelsbankenSessionStorage(new SessionStorage(),
                configuration);
        HandelsbankenSEApiClient client = spy(new HandelsbankenSEApiClient(new TinkHttpClient(),
                configuration));
        specificationResponseCaptor = new ResultCaptor<>();
        doAnswer(specificationResponseCaptor).when(client)
                .createTransfer(any(Creatable.class), any(TransferSpecificationRequest.class));
        new BankIdAuthenticationController<>(
                mock(AgentContext.class),
                new HandelsbankenBankIdAuthenticator(client,
                        credentials, persistentStorage, sessionStorage)
        ).authenticate(credentials);

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifier.Type.SE_SHB_INTERNAL,
                HandelsbankenSETestConfig.HEDBERG_ACCOUNT));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SE, destinationAccountNumber));
        transfer.setAmount(Amount.inSEK(1.1284d));

        TransferMessageFormatter messageFormatter = mock(TransferMessageFormatter.class);
        when(messageFormatter.getMessages(eq(transfer), anyBoolean()))
                .thenReturn(new Messages("Just a test", "Test transfer"));

        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("Translated error text");
        new HandelsbankenSEBankTransferExecutor(client, sessionStorage,
                new ExecutorExceptionResolver(catalog), messageFormatter)
                .executeTransfer(transfer);
    }

    private void assertHandelsbankenHasConfirmedInformationForSigning() {
        TransferSpecificationResponse actual = specificationResponseCaptor.getActual();
        assertThat(actual, notNullValue());
        assertThat(actual.getStatus(), is("DRAFT"));
        HandelsbankenAmount returnedAmount = actual.getAmount();
        assertThat(returnedAmount, notNullValue());
        assertThat(returnedAmount.asDouble(), is(1.13));
        HandelsbankenSEPaymentAccount fromAccount = actual.getFromAccount();
        assertThat(fromAccount, notNullValue());
        assertThat(fromAccount.hasIdentifier(HandelsbankenSETestConfig.HEDBERG_ACCOUNT), is(true));
        HandelsbankenSEPaymentAccount toAccount = actual.getToAccount();
        assertThat(toAccount, notNullValue());
        assertThat(toAccount.hasIdentifier(destinationWithoutClearingNumber()), is(true));
    }

    private String destinationWithoutClearingNumber() {
        return destinationAccountNumber.substring(4);
    }
}
