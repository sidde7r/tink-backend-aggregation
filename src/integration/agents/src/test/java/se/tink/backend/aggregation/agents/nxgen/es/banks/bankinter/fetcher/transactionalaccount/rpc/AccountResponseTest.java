package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import static org.junit.Assert.assertEquals;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.TEST_DATA_IBAN;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountResponseTest {

    @Test
    public void testAccountResponse() {
        final AccountResponse accountResponse =
                loadTestResponse("2.get_movimientos_cuenta_0.xhtml", AccountResponse.class);
        final JsfUpdateResponse accountInfo =
                loadTestResponse(
                        "3.movimientos-cabecera-head-datos-detalle.xhtml", JsfUpdateResponse.class);

        final TransactionalAccount account =
                accountResponse
                        .toTinkAccount(
                                "/extracto/secure/movimientos_cuenta.xhtml?INDEX_CTA=0&IND=N",
                                accountInfo)
                        .orElse(null);
        assertEquals(
                "/extracto/secure/movimientos_cuenta.xhtml?INDEX_CTA=0&IND=N",
                account.getApiIdentifier());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(TEST_DATA_IBAN, account.getIdentifiers().get(0).getIdentifier());
        assertEquals(TEST_DATA_IBAN, account.getIdModule().getAccountNumber());
        assertEquals(TEST_DATA_IBAN, account.getIdModule().getUniqueId());
        assertEquals("Cuenta nómina", account.getIdModule().getAccountName());
        assertEquals(AccountTypes.CHECKING, account.getType());
        // FIXME: account only holds the first holder name
        assertEquals(31337.42, account.getExactBalance().getDoubleValue(), 0.001);
    }
}
