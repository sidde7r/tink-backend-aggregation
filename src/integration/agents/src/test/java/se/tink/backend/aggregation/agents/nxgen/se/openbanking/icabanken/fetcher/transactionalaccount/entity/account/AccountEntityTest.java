package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {
    private AccountEntity accountEntity;

    @Test
    public void shouldMapToTinkAccount() {
        // given
        accountEntity = getAccountEntity();

        // when
        Optional<TransactionalAccount> optionalTransactionalAccount = accountEntity.toTinkModel();

        // then
        assertTrue(optionalTransactionalAccount.isPresent());
        TransactionalAccount transactionalAccount = optionalTransactionalAccount.get();
        assertEquals("98765432109", transactionalAccount.getIdModule().getUniqueId());
        assertEquals("98765432109", transactionalAccount.getIdModule().getAccountNumber());
        assertEquals("ICA KONTO", transactionalAccount.getIdModule().getAccountName());
        assertEquals("98765432109", transactionalAccount.getApiIdentifier());
        assertEquals(AccountTypes.CHECKING, transactionalAccount.getType());
        assertEquals(
                BigDecimal.valueOf(60672.36),
                transactionalAccount.getExactBalance().getExactValue());
        assertEquals("SEK", transactionalAccount.getExactBalance().getCurrencyCode());
        assertEquals(2, transactionalAccount.getIdentifiersAsList().size());
        assertEquals("Firstname Lastname", transactionalAccount.getHolderName().toString());
    }

    @Test
    public void shouldMapZeroToTinkModelWhenEmptyBalanceResponse() {
        // given
        accountEntity = getAccountEntityWithEmptyBalanceResponse();

        // when
        Optional<TransactionalAccount> optionalTransactionalAccount = accountEntity.toTinkModel();

        // then
        assertTrue(optionalTransactionalAccount.isPresent());
        TransactionalAccount transactionalAccount = optionalTransactionalAccount.get();
        assertEquals(BigDecimal.valueOf(0), transactionalAccount.getExactBalance().getExactValue());
    }

    private AccountEntity getAccountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "   \"resourceId\": \"98765432109\",\n"
                        + "   \"iban\": \"SE6792700000098765432109\",\n"
                        + "   \"bban\": \"98765432109\",\n"
                        + "   \"currency\": \"SEK\",\n"
                        + "   \"name\": \"ICA KONTO\",\n"
                        + "   \"bic\": \"IBCASES1\",\n"
                        + "   \"balances\": [\n"
                        + "    {\n"
                        + "     \"balanceAmount\": {\n"
                        + "      \"currency\": \"SEK\",\n"
                        + "      \"amount\": 60672.36\n"
                        + "     },\n"
                        + "     \"balanceType\": \"interimAvailable\",\n"
                        + "     \"creditLimitIncluded\": false\n"
                        + "    },\n"
                        + "    {\n"
                        + "     \"balanceAmount\": {\n"
                        + "      \"currency\": \"SEK\",\n"
                        + "      \"amount\": 60573.36\n"
                        + "     },\n"
                        + "     \"balanceType\": \"expected\",\n"
                        + "     \"creditLimitIncluded\": false\n"
                        + "    }\n"
                        + "   ],\n"
                        + "   \"owner\": [\n"
                        + "    \"Firstname Lastname\"\n"
                        + "   ]\n"
                        + "  }",
                AccountEntity.class);
    }

    private AccountEntity getAccountEntityWithEmptyBalanceResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "   \"resourceId\": \"98765432109\",\n"
                        + "   \"iban\": \"SE6792700000098765432109\",\n"
                        + "   \"bban\": \"98765432109\",\n"
                        + "   \"currency\": \"SEK\",\n"
                        + "   \"name\": \"ICA KONTO\",\n"
                        + "   \"bic\": \"IBCASES1\",\n"
                        + "   \"balances\": [\n"
                        + "   ],\n"
                        + "   \"owner\": [\n"
                        + "    \"Firstname Lastname\"\n"
                        + "   ]\n"
                        + "  }",
                AccountEntity.class);
    }
}
