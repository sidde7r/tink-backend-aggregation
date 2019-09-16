package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig.AMOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig.ERROR_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig.PAYMENT_DESTINATION_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig.PAYMENT_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig.SOURCE_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig.S_NO;

import java.util.Collections;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.transfer.rpc.Transfer;

/** Careful with this test! Actual Payments made! */
public class HandelsbankenSEBankPaymentExecutorTest {

    private String destinationAccountNumber;

    @Test
    public void canDoPayment() throws Exception {
        destinationAccountNumber = PAYMENT_DESTINATION_ACCOUNT;
        doPayment(PAYMENT_MESSAGE);
    }

    private void doPayment(String message)
            throws se.tink.backend.aggregation.agents.exceptions.AuthenticationException,
                    se.tink.backend.aggregation.agents.exceptions.AuthorizationException {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setField(Field.Key.USERNAME, S_NO);

        HandelsbankenSEConfiguration configuration = new HandelsbankenSEConfiguration();
        HandelsbankenPersistentStorage persistentStorage =
                new HandelsbankenPersistentStorage(new PersistentStorage(), Collections.emptyMap());
        HandelsbankenSessionStorage sessionStorage =
                new HandelsbankenSessionStorage(new SessionStorage(), configuration);

        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SE_SHB_INTERNAL, SOURCE_ACCOUNT));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SE_BG, destinationAccountNumber));
        transfer.setAmount(Amount.inSEK(AMOUNT));
        transfer.setDestinationMessage(message);

        HandelsbankenSEApiClient client =
                spy(new HandelsbankenSEApiClient(new TinkHttpClient(), configuration));
        new BankIdAuthenticationController<>(
                        new AgentTestContext(credentials),
                        new HandelsbankenBankIdAuthenticator(
                                client, credentials, persistentStorage, sessionStorage),
                        new PersistentStorage(),
                        credentials)
                .authenticate(credentials);

        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn(ERROR_MESSAGE);
//        HandelsbankenSEPaymentExecutor executor =
//                new HandelsbankenSEPaymentExecutor(
//                        supplementalclient, sessionStorage, new ExecutorExceptionResolver(catalog));
//
//        executor.executePayment(transfer);
    }
}
