package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityTest {

    @Test
    public void shouldReturnRemittanceInformationUnstructured() {
        // given
        TransactionEntity transactionEntity =
                getTransactionEntityWithRemittanceInformationUnstructured();

        // then
        assertEquals(
                "remittanceInformationUnstructured",
                ReflectionTestUtils.invokeMethod(transactionEntity, "getTinkDescription"));
    }

    @Test
    public void shouldReturnMerchantName() {
        // given
        TransactionEntity transactionEntity =
                getTransactionEntityWithoutRemittanceInformationUnstructured();

        // then
        assertEquals(
                "merchantName",
                ReflectionTestUtils.invokeMethod(transactionEntity, "getTinkDescription"));
    }

    @Test
    public void shouldReturnText() {
        // given
        TransactionEntity transactionEntity =
                getTransactionEntityWithoutRemittanceInformationUnstructuredAndWithoutMerchantName();

        // then
        assertEquals(
                "Köp", ReflectionTestUtils.invokeMethod(transactionEntity, "getTinkDescription"));
    }

    @Test
    public void shouldReturnNullDescription() {
        // given
        TransactionEntity transactionEntity = empty();

        // then
        assertNull(ReflectionTestUtils.invokeMethod(transactionEntity, "getTinkDescription"));
    }

    private TransactionEntity empty() {
        return SerializationUtils.deserializeFromString("{}", TransactionEntity.class);
    }

    private TransactionEntity getTransactionEntityWithoutRemittanceInformationUnstructured() {
        return SerializationUtils.deserializeFromString(
                "{\"entryReference\":\"2019501730151122\",\"text\":\"Köp\",\"merchantName\":\"merchantName\",\"transactionDate\":\"2020-07-11\",\"transactionAmount\":{\"amount\":-220.00,\"currency\":\"SEK\"}}",
                TransactionEntity.class);
    }

    private TransactionEntity getTransactionEntityWithRemittanceInformationUnstructured() {
        return SerializationUtils.deserializeFromString(
                "{\"remittanceInformationUnstructured\":\"remittanceInformationUnstructured\",\"entryReference\":\"2019501730151122\",\"text\":\"Köp\",\"merchantName\":\"merchantName\",\"transactionDate\":\"2020-07-11\",\"transactionAmount\":{\"amount\":-220.00,\"currency\":\"SEK\"}}",
                TransactionEntity.class);
    }

    private TransactionEntity
            getTransactionEntityWithoutRemittanceInformationUnstructuredAndWithoutMerchantName() {
        return SerializationUtils.deserializeFromString(
                "{\"entryReference\":\"2019501730151122\",\"text\":\"Köp\",\"transactionDate\":\"2020-07-11\",\"transactionAmount\":{\"amount\":-220.00,\"currency\":\"SEK\"}}",
                TransactionEntity.class);
    }
}
