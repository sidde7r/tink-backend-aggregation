package se.tink.backend.aggregation.agents.banks;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.PersistentSession;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.TransactionEntity;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.utils.Iterators;
import se.tink.backend.common.utils.TestSSN;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.IterableUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SkandiabankenAgentTest extends AbstractAgentTest<SkandiabankenAgent> {

    public SkandiabankenAgentTest() {
        super(SkandiabankenAgent.class);
    }

    @Test
    public void testUser1Password() throws Exception {
        testAgent(TestSSN.FH, "2473");
    }

    @Test
    public void testUser2PasswordAuthenticationError() throws Exception {
        testAgentAuthenticationError("198203300382", "2473");
    }

    @Test
    public void testUser1BankId() throws Exception {
        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testFredrikWithMobilebankIdPersistentLoggedIn() throws Exception {

        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.FH);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Test
    public void testUser2BankIdAuthenticationError() throws Exception {
        testAgentAuthenticationError("198203300382", null, CredentialsTypes.MOBILE_BANKID);
    }

    private Ordering<TransactionEntity> settledOrder = Ordering.natural().onResultOf(TransactionEntity::getSettled);
    private Ordering<TransactionEntity> timestampOrder = Ordering.natural().onResultOf(TransactionEntity::getTimestamp);

    /**
     * Simulate incoming cycled transactions and how to handle them.
     */
    @Test
    public void testPrefixDeduplication() {
        final int MAX_NUMBER_OF_TRANSACTIONS = 500;
        final int TRANSACTIONS_PER_PAGE = 20;
        final Random random = new Random(0);

        for (int testRun = 0; testRun < 5000; testRun++) {
            int numberOfTransactions = random.nextInt(MAX_NUMBER_OF_TRANSACTIONS);

            ArrayList<Long> allTransactions = Lists.newArrayList(com.google.common.collect.Iterators.limit(
                    Iterators.counter(), numberOfTransactions));
            Iterator<Long> infiniteTransactionStream = com.google.common.collect.Iterators.cycle(allTransactions);

            List<Long> accumulatedTransactions = Lists.newArrayList();
            UnmodifiableIterator<List<Long>> transactionPages = com.google.common.collect.Iterators.partition(
                    infiniteTransactionStream, TRANSACTIONS_PER_PAGE);

            outerLoop:
            while (transactionPages.hasNext()) {

                List<Long> page = transactionPages.next();
                for (int i = 0; i < page.size(); i++) {
                    if (accumulatedTransactions.size() > 0
                            && IterableUtils.sharePrefixes(accumulatedTransactions, page.subList(i, page.size()))) {
                        break outerLoop;
                    }

                    accumulatedTransactions.add(page.get(i));

                }

            }

            Assert.assertEquals(allTransactions, accumulatedTransactions);
        }
    }

    @Test
    public void testTransactionSinglePage() throws IOException {
        List<TransactionEntity> accumulatedTransactions = Lists.newArrayList();
        List<Transaction> parsedAccumulatedTransactions = Lists.newArrayList();
        testTransactionParsing("data/test/skandiabanken-parse-test.json", parsedAccumulatedTransactions,
                accumulatedTransactions);
    }

    @Test
    public void testTransactionMultiPage() throws IOException {
        List<TransactionEntity> accumulatedTransactions = Lists.newArrayList();
        List<Transaction> parsedAccumulatedTransactions = Lists.newArrayList();
        testTransactionParsing("data/test/skandiabanken-parse-test.json", parsedAccumulatedTransactions,
                accumulatedTransactions);
        testTransactionParsing("data/test/skandiabanken-parse-test2.json", parsedAccumulatedTransactions,
                accumulatedTransactions);
    }

    public void testTransactionParsing(String filePath, List<Transaction> parsedAccumulatedTransactions,
            List<TransactionEntity> accumulatedTransactions) throws IOException {

        byte[] data = Files.readAllBytes(new File(filePath).toPath());
        AccountEntity accountEntity = SerializationUtils.deserializeFromString(new String(data, Charsets.UTF_8),
                AccountEntity.class);

        List<TransactionEntity> transactionEntities = accountEntity.getTransactions();
        Assert.assertEquals(transactionEntities.size(), 20);

        // Make sure I've understood all the ordering correct.

        Assert.assertFalse(settledOrder.isOrdered(transactionEntities));
        Assert.assertFalse(settledOrder.reverse().isOrdered(transactionEntities));
        Assert.assertFalse(timestampOrder.isOrdered(transactionEntities));
        Assert.assertFalse(timestampOrder.reverse().isOrdered(transactionEntities));

        // The prefix bailing.

        for (int i = 0; i < transactionEntities.size(); i++) {
            TransactionEntity transactionEntity = transactionEntities.get(i);

            if (accumulatedTransactions.size() > 0
                    && IterableUtils.sharePrefixes(accumulatedTransactions,
                    transactionEntities.subList(i, transactionEntities.size())))
            // If the rest of the tail of the paged transactions are coincide with the prefix of the accumulated
            // transactions.
            {
                break;
            }
            accumulatedTransactions.add(transactionEntity);

            final Transaction transaction = CoreTransactionMapper
                    .toCoreTransaction(SkandiabankenAgent.parseAccountTransaction(transactionEntity));
            parsedAccumulatedTransactions.add(transaction);

        }

        // We fail here now.
        Assert.assertEquals(20, accumulatedTransactions.size());

    }

    @Test
    public void testDescriptionFormatting() {

        // Descriptions that should be changed
        assertDescriptionFormatting("Willys\\\\Linköping\\", "Willys, Linköping");
        assertDescriptionFormatting("ALI BLOMMOR\\\\Göteborg\\", "ALI BLOMMOR, Göteborg");
        assertDescriptionFormatting("Hemkop Solna Ulriksdal\\\\Solna\\", "Hemkop Solna Ulriksdal, Solna");

        // Descriptions that doesn't get changed
        assertDescriptionFormatting("ALI BLOMMOR, GOTEBORG", "ALI BLOMMOR, GOTEBORG");
        assertDescriptionFormatting("SJ INTERNETBOK", "SJ INTERNETBOK");
        assertDescriptionFormatting("http://www.viaplay.se", "http://www.viaplay.se");
        assertDescriptionFormatting("BLOCKET/", "BLOCKET/");
    }

    public void assertDescriptionFormatting(String input, String expectedResult) {
        TransactionEntity t = new TransactionEntity();
        t.setMerchant(input);

        Assert.assertEquals(expectedResult, t.getMerchant());
    }

    @Test
    public void testPersistentSessionSerialization() throws Exception {

        PersistentSession session = new PersistentSession();
        session.setCustomerId(1234567);
        session.addCookie(new BasicClientCookie("name", "value"));

        String serialized = MAPPER.writeValueAsString(session);

        PersistentSession result = MAPPER.readValue(serialized, PersistentSession.class);

        Assert.assertEquals(session.getCustomerId(), result.getCustomerId());
        Assert.assertEquals(1, result.getCookies().size());
    }

    @Test
    public void testPersistentLoginExpiredSession() throws Exception {

        PersistentSession session = new PersistentSession();
        session.setCustomerId(1234567);

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setPersistentSession(session);

        testAgentPersistentLoggedInExpiredSession(credentials, PersistentSession.class);
    }
}
