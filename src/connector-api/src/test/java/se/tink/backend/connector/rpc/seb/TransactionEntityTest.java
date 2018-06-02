package se.tink.backend.connector.rpc.seb;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.TransactionTypes;

public class TransactionEntityTest {

    private TransactionEntity entity;

    @Before
    public void setUp() {
        entity = new TransactionEntity();
        entity.setAmount(-30d);
        entity.setDescription("someDescription");
        entity.setDate(new Date());
        entity.setExternalId("someExternalId");
        entity.setType(TransactionTypes.DEFAULT);
    }

    @Test
    public void isPendingTransactionValid() throws Exception {
        entity.setStatus(TransactionStatus.RESERVED);
        Assert.assertTrue(entity.isValid("someExternalUserId"));
    }

    @Test
    public void isPendingTransactionValidWithExpirationDate() throws Exception {
        entity.setStatus(TransactionStatus.RESERVED);
        entity.setPayload(
                ImmutableMap.of(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE, new Date().getTime()));

        Assert.assertTrue(entity.isValid("someExternalUserId"));
    }

    @Test
    public void isNonPendingTransactionNotValidWithExpirationDate() throws Exception {
        entity.setStatus(TransactionStatus.BOOKED);
        entity.setPayload(
                ImmutableMap.of(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE, new Date().getTime()));

        Assert.assertFalse(entity.isValid("someExternalUserId"));
    }

    @Test
    public void isNonPendingTransactionValidWithoutExpirationDate() throws Exception {
        entity.setStatus(TransactionStatus.RESERVED);

        Assert.assertTrue(entity.isValid("someExternalUserId"));
    }

    @Test
    public void isPendingTransactionNotValidWithWrongExpirationDateType() throws Exception {
        entity.setStatus(TransactionStatus.RESERVED);
        entity.setPayload(ImmutableMap.of(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE, "[]"));

        Assert.assertFalse(entity.isValid("someExternalUserId"));
    }
}
