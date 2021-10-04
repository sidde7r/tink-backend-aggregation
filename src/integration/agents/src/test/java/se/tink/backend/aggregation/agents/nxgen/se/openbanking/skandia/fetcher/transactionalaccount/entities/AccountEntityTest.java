package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {

    @Test
    public void shouldMapToTinkAccount() {
        // given
        AccountEntity accountEntity = getAccountEntity();
        List<BalanceEntity> balances = getBalanceResponse().getBalances();
        accountEntity.setBalances(balances);

        // when
        Optional<TransactionalAccount> optionalTransactionalAccount = accountEntity.toTinkAccount();

        // then
        assertTrue(optionalTransactionalAccount.isPresent());
        TransactionalAccount transactionalAccount = optionalTransactionalAccount.get();
        assertEquals("91591111111", transactionalAccount.getIdModule().getUniqueId());
        assertEquals("91591111111", transactionalAccount.getIdModule().getAccountNumber());
        assertEquals("Allt-i-Ett konto", transactionalAccount.getIdModule().getAccountName());
        assertEquals("915011111111111", transactionalAccount.getApiIdentifier());
        assertEquals(
                BigDecimal.valueOf(5000.99),
                transactionalAccount.getExactBalance().getExactValue());
        assertEquals("SEK", transactionalAccount.getExactBalance().getCurrencyCode());
        assertEquals(2, transactionalAccount.getIdentifiers().size());
    }

    private AccountEntity getAccountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\"resourceId\":\"915011111111111\",\"bban\":\"91591111111\",\"bic\":\"SKIASESS\",\"cashAccountType\":\"CACC\",\"currency\":\"SEK\",\"iban\":\"SE7991500006025111111111\",\"name\":\"Allt-i-Ett konto\",\"usage\":\"PRIV\",\"_links\":{\"self\":{\"href\":\"/v1/accounts/915011111111111\"},\"balances\":{\"href\":\"/v1/accounts/915011111111111/balances\"},\"transactions\":{\"href\":\"/v1/accounts/915011111111111/transactions\"}}}\n",
                AccountEntity.class);
    }

    private GetBalancesResponse getBalanceResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"account\":{\"iban\":\"SE7991500006025111111111\",\"currency\":\"SEK\"},\"balances\":[{\"balanceAmount\":{\"amount\":\"5000.99\",\"currency\":\"SEK\"},\"balanceType\":\"closingbooked\",\"referenceDate\":\"2021-04-13T00:00:00\"},{\"balanceAmount\":{\"amount\":\"5000.99\",\"currency\":\"SEK\"},\"balanceType\":\"available\",\"referenceDate\":\"2021-04-13T00:00:00\"}]}\n",
                GetBalancesResponse.class);
    }
}
