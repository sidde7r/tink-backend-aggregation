package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual.HandelsbankenSETestConfig.AMOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual.HandelsbankenSETestConfig.ERROR_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual.HandelsbankenSETestConfig.E_APPROVAL_ID;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual.HandelsbankenSETestConfig.E_INVOICE_DESTINATION_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual.HandelsbankenSETestConfig.E_INVOICE_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual.HandelsbankenSETestConfig.SOURCE_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual.HandelsbankenSETestConfig.S_NO;

import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

/** Careful with this test! Actual EInvoice approvals made! */
public class HandelsbankenSEBankEInvoiceExecutorTest {

    private String destinationAccountNumber;

    @Test
    public void canPayEInvoice() throws Exception {
        destinationAccountNumber = E_INVOICE_DESTINATION_ACCOUNT;
        payEInvoice(E_INVOICE_MESSAGE);
    }

    private void payEInvoice(String message)
            throws se.tink.backend.aggregation.agents.exceptions.AuthenticationException,
                    se.tink.backend.aggregation.agents.exceptions.AuthorizationException {

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setField(Field.Key.USERNAME, S_NO);

        HandelsbankenSEConfiguration configuration = new HandelsbankenSEConfiguration();
        HandelsbankenPersistentStorage persistentStorage =
                new HandelsbankenPersistentStorage(new PersistentStorage(), Collections.emptyMap());
        HandelsbankenSessionStorage sessionStorage =
                new HandelsbankenSessionStorage(
                        configuration, new SessionStorage(), new FakeLogMasker());

        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SE_SHB_INTERNAL, SOURCE_ACCOUNT));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SE_BG, destinationAccountNumber));
        transfer.setAmount(Amount.inSEK(AMOUNT));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(message);
        transfer.setRemittanceInformation(remittanceInformation);

        String serializedTransfer = SerializationUtils.serializeToString(transfer);

        HashMap<TransferPayloadType, String> payload = new HashMap<>(2);
        payload.put(TransferPayloadType.PROVIDER_UNIQUE_ID, E_APPROVAL_ID);
        payload.put(TransferPayloadType.ORIGINAL_TRANSFER, serializedTransfer);
        transfer.setPayload(payload);

        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn(ERROR_MESSAGE);

        HandelsbankenSEApiClient client =
                spy(new HandelsbankenSEApiClient(new LegacyTinkHttpClient(), configuration));
        new BankIdAuthenticationController<>(
                        new AgentTestContext(credentials),
                        new HandelsbankenBankIdAuthenticator(
                                client, credentials, persistentStorage, sessionStorage),
                        new PersistentStorage(),
                        credentials)
                .authenticate(credentials);

        //        HandelsbankenSEEInvoiceExecutor seeInvoiceExecutor =
        //                new HandelsbankenSEEInvoiceExecutor(
        //                        client, sessionStorage, new ExecutorExceptionResolver(catalog));
        //
        //        seeInvoiceExecutor.approveEInvoice(transfer);
    }
}
