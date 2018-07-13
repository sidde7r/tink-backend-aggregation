package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionAccountEntityTest {
    private static final String SERIALIZED_TRANSACTION_ACCOUNT_ENTITY = "{\"scopes\":[\"PAYMENT_FROM\",\"TRANSFER_TO\"],\"currencyCode\":\"SEK\",\"amount\":\"200\",\"name\":\"Privatkonto\",\"id\":\"***\",\"accountNumber\":\"1234567890\",\"clearingNumber\":\"1234\",\"fullyFormattedNumber\":\"1234567890\"}";

    @Test
    public void deserialize() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TransactionAccountEntity transactionAccountEntity = objectMapper.readValue(SERIALIZED_TRANSACTION_ACCOUNT_ENTITY, TransactionAccountEntity.class);

        Set<TransactionAccountEntity.AccountScope> scopes = transactionAccountEntity.getScopes();
        assertThat(scopes).doesNotContain(TransactionAccountEntity.AccountScope.TRANSFER_FROM);
        assertThat(scopes).contains(TransactionAccountEntity.AccountScope.TRANSFER_TO);
        assertThat(scopes).contains(TransactionAccountEntity.AccountScope.PAYMENT_FROM);
        assertThat(transactionAccountEntity.getAmount()).isEqualTo("200");
        assertThat(transactionAccountEntity.getClearingNumber()).isEqualTo("1234");
        assertThat(transactionAccountEntity.getFullyFormattedNumber()).isEqualTo("1234567890");
    }

    @Test
    public void verifyTrueWhenScopeInScopeList() {
        TransactionAccountEntity transactionAccountEntity = new TransactionAccountEntity();
        transactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_FROM));
        assertThat(transactionAccountEntity.hasScopeInScopeList(TransactionAccountEntity.AccountScope.TRANSFER_FROM)).isTrue();
    }

    @Test
    public void verifyFalseWhenNotInScopeList() {
        TransactionAccountEntity transactionAccountEntity = new TransactionAccountEntity();
        transactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_FROM));
        assertThat(transactionAccountEntity.hasScopeInScopeList(TransactionAccountEntity.AccountScope.TRANSFER_TO)).isFalse();
    }
}
