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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.amount.Amount;

public class BankinterEntitiesParsingTest {
    final String TEST_DATA_PATH = "data/test/agents/es/bankinter/";
    final String TEST_DATA_IBAN = "ES2201281337857486299388";
    private static final SimpleDateFormat TRANSACTION_DATE_FORMATTER =
            new SimpleDateFormat("dd/MM/yyyy");

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

    private void assertTransaction(
            String date, String description, double eur, Transaction transaction) {
        assertEquals(date, TRANSACTION_DATE_FORMATTER.format(transaction.getDate()));
        assertEquals(description, transaction.getDescription());
        assertEquals(Amount.inEUR(eur), transaction.getAmount());
    }

    @Test
    public void testTransactionsResponse() {
        final TransactionsResponse transactionsResponse =
                loadTestResponse("4.transactions.xhtml", TransactionsResponse.class);

        List<Transaction> transactions =
                transactionsResponse.toTinkTransactions().stream().collect(Collectors.toList());

        assertEquals(13, transactions.size());
        assertTransaction(
                "10/06/2019", "Pago Bizum De Mengano;ramirez;tal", 3.5, transactions.get(0));
        assertTransaction(
                "10/06/2019", "Pago Bizum De Maria Luisa;garcia", 3.5, transactions.get(1));
        assertTransaction(
                "07/06/2019", "Pago Bizum De Hermangarda;perez De", 3.5, transactions.get(2));
        assertTransaction(
                "07/06/2019", "Pago Bizum De Manuel Francisco;gon", 3.5, transactions.get(3));
        assertTransaction("07/06/2019", "Pago Bizum De David;marin", 3.5, transactions.get(4));
        assertTransaction("07/06/2019", "Pago Bizum De Samuel;delgad", 3.5, transactions.get(5));
        assertTransaction("07/06/2019", "Pago Bizum De Marina;soler;v", 3.5, transactions.get(6));
        assertTransaction(
                "05/06/2019", "Trans /ministerio de Educacion", 3019.21, transactions.get(7));
        assertTransaction("04/06/2019", "Recibo Visa Clasica", -238.12, transactions.get(8));
        assertTransaction("04/06/2019", "Trans /perez De tal Z", 500, transactions.get(9));
        assertTransaction(
                "04/06/2019", "Trans /ministerio de Educacion", 1337.42, transactions.get(10));
        assertTransaction(
                "03/06/2019", "Recib /c.p. Rufino Blanco 42", -312.25, transactions.get(11));
        assertTransaction("03/06/2019", "Recibo /qualitas", -115.12, transactions.get(12));

        final PaginationKey nextKey = transactionsResponse.getNextKey(0);
        assertEquals("j_id374401928_5f006346:j_id374401928_5f006392", nextKey.getSource());
    }

    @Test
    public void testNoTransactionsResponse() {
        final TransactionsResponse transactionsResponse =
                loadTestResponse("5.transactions_none.xhtml", TransactionsResponse.class);

        Collection<Transaction> transactions = transactionsResponse.toTinkTransactions();

        assertEquals(0, transactions.size());

        final PaginationKey nextKey = transactionsResponse.getNextKey(0);
        assertEquals(1, nextKey.getConsecutiveEmptyReplies());
        assertEquals("j_id374401928_5f006346:j_id374401928_5f006392", nextKey.getSource());
    }
}
