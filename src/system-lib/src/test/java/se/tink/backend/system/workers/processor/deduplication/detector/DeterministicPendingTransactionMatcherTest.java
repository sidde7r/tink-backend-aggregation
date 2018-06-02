package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.connector.rpc.PartnerTransactionPayload;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DeterministicPendingTransactionMatcherTest {

    private String tId1 = UUIDUtils.generateUUID();
    private String tId2 = UUIDUtils.generateUUID();
    private String tId3 = UUIDUtils.generateUUID();
    private String tId4 = UUIDUtils.generateUUID();
    private String tId5 = UUIDUtils.generateUUID();
    private String tId6 = UUIDUtils.generateUUID();
    private String tId7 = UUIDUtils.generateUUID();
    private String tId8 = UUIDUtils.generateUUID();
    private String tId9 = UUIDUtils.generateUUID();
    private String tId10 = UUIDUtils.generateUUID();

    private String aId1 = UUIDUtils.generateUUID();
    private String aId2 = UUIDUtils.generateUUID();
    private String aId3 = UUIDUtils.generateUUID();

    private List<Transaction> inStore;
    private List<Transaction> inBatch;

    @Before
    public void setUp() {
        inStore = createInStoreTransactions();
        inBatch = Lists.newArrayList();
    }

    @Test
    public void findExpiredPendingOnAccount1() throws Exception {
        DeterministicPendingTransactionMatcher matcher = new DeterministicPendingTransactionMatcher(inStore, inBatch);

        List<Transaction> expiredPending = matcher.findExpiredPending(aId1, Lists.newArrayList());

        Assert.assertEquals(1, expiredPending.size());
        Assert.assertEquals(tId2, expiredPending.get(0).getId());
    }

    @Test
    public void findExpiredPendingOnAccount2() throws Exception {
        DeterministicPendingTransactionMatcher matcher = new DeterministicPendingTransactionMatcher(inStore, inBatch);

        List<Transaction> expiredPending = matcher.findExpiredPending(aId2, Lists.newArrayList());

        Assert.assertEquals(1, expiredPending.size());
        Assert.assertEquals(tId6, expiredPending.get(0).getId());
    }

    @Test
    public void findExpiredPendingOnAccount3() throws Exception {
        DeterministicPendingTransactionMatcher matcher = new DeterministicPendingTransactionMatcher(inStore, inBatch);

        List<Transaction> expiredPending = matcher.findExpiredPending(aId3, Lists.newArrayList());

        Assert.assertEquals(0, expiredPending.size());
    }

    @Test
    public void findExpiredPendingWhereExpiredAlreadyInDeleteList() throws Exception {
        Transaction t2 = new Transaction();
        t2.setId(tId2);
        DeterministicPendingTransactionMatcher matcher = new DeterministicPendingTransactionMatcher(inStore, inBatch);

        List<Transaction> expiredPending = matcher.findExpiredPending(aId1, Lists.newArrayList(t2));

        Assert.assertEquals(0, expiredPending.size());
    }

    private List<Transaction> createInStoreTransactions() {
        List<Transaction> trx = Lists.newArrayList();
        trx.add(createTransaction(tId1, aId1, true, null));
        trx.add(createTransaction(tId2, aId1,true, DateUtils.addDays(new Date(), -1)));
        trx.add(createTransaction(tId3, aId1,true, DateUtils.addDays(new Date(), 1)));
        trx.add(createTransaction(tId4, aId1,false, DateUtils.addDays(new Date(), -1)));
        trx.add(createTransaction(tId5, aId1,false, DateUtils.addDays(new Date(), 1)));
        trx.add(createTransaction(tId6, aId2,true, DateUtils.addDays(new Date(), -100)));
        trx.add(createTransaction(tId7, aId2,false, null));
        trx.add(createTransaction(tId8, aId3,true, DateUtils.addDays(new Date(), 1)));
        trx.add(createTransaction(tId9, aId3,true, null));
        trx.add(createTransaction(tId10, aId3,false, null));
        return trx;
    }

    private Transaction createTransaction(String id, String accountId, boolean pending, Date expirationDate) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setAccountId(accountId);
        t.setPending(pending);

        if (expirationDate != null) {
            Map<String, Object> payload = ImmutableMap.of(
                    PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE, expirationDate);
            String serializedPayload = SerializationUtils.serializeToString(payload);
            t.setInternalPayload(Transaction.InternalPayloadKeys.PARTNER_PAYLOAD, serializedPayload);
        }
        return t;
    }
}
