package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.amount.Amount;

public class BankinterEntitiesParsingTest {
    final String TEST_DATA_PATH = "data/test/agents/es/bankinter/";
    final String TEST_DATA_IBAN = "ES2201281337857486299388";

    private <C> C loadTestResponse(String path, Class<C> responseClass) {
        try {
            final byte[] bytes = Files.readAllBytes(Paths.get(TEST_DATA_PATH, path));
            final String bodyString = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            final InputStream bodyStream = new ByteArrayInputStream(bytes);
            HttpResponse mockedResponse = mock(HttpResponse.class);
            doReturn(bodyString).when(mockedResponse).getBody(String.class);
            doReturn(bodyStream).when(mockedResponse).getBodyInputStream();
            if (responseClass == HttpResponse.class) {
                return (C) mockedResponse;
            } else {
                Constructor constructor = responseClass.getConstructor(HttpResponse.class);
                return (C) constructor.newInstance(mockedResponse);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not load test response: " + path, e);
        }
    }

    @Test
    public void testGlobalPositionResponse() {
        GlobalPositionResponse response =
                loadTestResponse("1.extracto_integral.xhtml", GlobalPositionResponse.class);

        assertEquals(1, response.getNumberOfAccounts());
        assertEquals(0, response.getAccountIds().get(0).intValue());
    }

    @Test
    public void testAccountResponse() {
        final AccountResponse accountResponse =
                loadTestResponse("2.get_movimientos_cuenta_0.xhtml", AccountResponse.class);
        final JsfUpdateResponse accountInfo =
                loadTestResponse(
                        "3.movimientos-cabecera-head-datos-detalle.xhtml", JsfUpdateResponse.class);

        final TransactionalAccount account = accountResponse.toTinkAccount(0, accountInfo);
        assertEquals("0", account.getApiIdentifier());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(TEST_DATA_IBAN, account.getIdentifiers().get(0).getIdentifier());
        assertEquals(TEST_DATA_IBAN, account.getIdModule().getAccountNumber());
        assertEquals(TEST_DATA_IBAN, account.getIdModule().getUniqueId());
        assertEquals("Cuenta nómina", account.getIdModule().getAccountName());
        // FIXME: account only holds the first holder name
        assertEquals(Amount.inEUR(31337.42), account.getBalance());
    }
}
