package se.tink.backend.combined.integration;

import com.google.api.client.util.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;

/**
 * Elasticsearch test to find similar transactions
 * are we testing Elastic search here ? :)
 * TODO this is a unit test
 */
public class SimilarTransactionsSearcherTest extends AbstractServiceIntegrationTest {

    private static Category groceriesCat;
    private static Category uncategorizedCat;
    private static Category transfersCat;

    @BeforeClass
    public static void before() {
        CategoryRepository categoryRepo = serviceContext.getRepository(CategoryRepository.class);
        groceriesCat = categoryRepo.findByCode(serviceContext.getCategoryConfiguration().getGroceriesCode());
        uncategorizedCat = categoryRepo.findByCode(serviceContext.getCategoryConfiguration().getExpenseUnknownCode());
        transfersCat = categoryRepo.findByCode(categoryConfiguration.getTransferUnknownCode());
    }

    @Test
    public void findSimilarTransactionsTest() {
        List<User> users = getTestUsers("findSimilarTransactionsTest");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = getTestTransactions(u.getId());
            for (Transaction t : transactions) {
                if (t.getDescription().contains("ICA")) {
                    t.setCategory(groceriesCat);
                } else {
                    t.setCategory(uncategorizedCat);
                }
            }
            transactionDao.saveAndIndex(u, transactions, true);
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "ICA");
            transaction.setCategory(groceriesCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());
            Assert.assertEquals(2, similarTransactions.size());

            log.info("Similar transactons are:");
            for (Transaction t : similarTransactions) {
                log.info(t.toString());
            }
        });
    }

    @Test
    public void findSimilarTransactionsShortWords() {
        List<User> users = getTestUsers("findSimilarTransactionsShortWordsTest");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = getTestTransactions(u.getId());
            for (Transaction t : transactions) {
                if (t.getDescription().contains("ICA")) {
                    t.setOriginalDescription("M E C K");
                    t.setDescription("M E C K");
                    t.setCategory(groceriesCat);
                } else {
                    t.setCategory(uncategorizedCat);
                }
            }
            transactionDao.saveAndIndex(u, transactions, true);
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "M E C K");
            transaction.setCategory(groceriesCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());
            Assert.assertEquals(2, similarTransactions.size());

            log.info("Similar transactons are:");
            for (Transaction t : similarTransactions) {
                log.info(t.toString());
            }
        });
    }

    @Test
    public void verify_dontMatchOnTooShortWord() {
        List<User> users = getTestUsers("dontMatchOnTooShortWord");
        users.forEach(u ->{

            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = getTestTransactions(u.getId());
            for (Transaction t : transactions) {
                if (t.getDescription().contains("ICA DALASTAN")) {
                    t.setOriginalDescription("Iz Vaderoter");
                    t.setDescription("Iz Vaderoter");
                    t.setCategory(groceriesCat);
                } else if (t.getDescription().contains("HAIR SOLUTION")) {
                    t.setOriginalDescription("Iz Bs Home");
                    t.setDescription("Iz Bs Home");
                    t.setCategory(groceriesCat);
                } else {
                    t.setCategory(uncategorizedCat);
                }
            }
            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "Iz Vaderoter");
            transaction.setCategory(groceriesCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());
            Assert.assertEquals(1, similarTransactions.size());
        });
    }

    @Test
    public void verify_matchOnTooShortWordIfOnlyShortWords() {
        List<User> users = getTestUsers("matchOnToohortWordIfOnlyShortWords");
        users.forEach(u ->{
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = getTestTransactions(u.getId());
            for (Transaction t : transactions) {
                if (t.getDescription().contains("ICA DALASTAN")) {
                    t.setOriginalDescription("Iz Vaderoter");
                    t.setDescription("Iz Vaderoter");
                    t.setCategory(groceriesCat);
                } else if (t.getDescription().contains("HAIR SOLUTION")) {
                    t.setOriginalDescription("Iz Bs Home");
                    t.setDescription("Iz Bs Home");
                    t.setCategory(groceriesCat);
                } else {
                    t.setCategory(uncategorizedCat);
                }
            }
            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "Iz");
            transaction.setCategory(groceriesCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());
            Assert.assertEquals(2, similarTransactions.size());
        });
    }

    @Test
    public void verify_matchOnThreeLetterWordIfLongest() {
        List<User> users = getTestUsers("matchOnThreeLetterWordIfLongest");
        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = getTestTransactions(u.getId());
            for (Transaction t : transactions) {
                if (t.getDescription().contains("ICA")) {
                    t.setOriginalDescription("Com Hem");
                    t.setDescription("Come Hem");
                    t.setCategory(groceriesCat);
                } else if (t.getDescription().contains("HAIR SOLUTION")) {
                    t.setOriginalDescription("Come Bort");
                    t.setDescription("Come Bort");
                    t.setCategory(groceriesCat);
                } else {
                    t.setCategory(uncategorizedCat);
                }
            }
            transactionDao.saveAndIndex(u, transactions, true);
            ;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "Com Hem");
            transaction.setCategory(groceriesCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());

            // There are two Com Hem transactions, they should match, not the Com Bort one.

            Assert.assertEquals(2, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsIncludingSwedishChars() {
        List<User> users = getTestUsers("similarTransactionsIncludingSwedishChars");
        users.forEach(u->{
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -2000, "Måller Bowl");
            t1.setCategory(uncategorizedCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -1500, "Måller Slö");
            t2.setCategory(uncategorizedCat);

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());
            Assert.assertEquals(1, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsExcludeSwishTrans() {
        List<User> users = getTestUsers("similarTransactionsIncludingSwedishChars");
        users.forEach(u ->{
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -2000, "Swish skickad +46701234567");
            t1.setCategory(uncategorizedCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -3000, "Swish skickad +46701236543");
            t2.setCategory(uncategorizedCat);

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());
            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsIncludeSwishTransWhenSameNumber() {
        List<User> users = getTestUsers("similarTransactionsIncludingSwedishChars");

        users.forEach( u ->{
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), 2000, "Swish skickad +46701234567");
            t1.setCategory(uncategorizedCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), 3000, "Swish skickad +46701234567");
            t2.setCategory(uncategorizedCat);

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());
            Assert.assertEquals(1, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsStopWordTest1() {
        List<User> users = getTestUsers("similarTransactionsStopWordTest1");

        users.forEach(u ->{
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -2000, "B aktiebolag");
            t1.setCategory(groceriesCat);
            transactions.add(t1);

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -1500, "H aktiebolag");
            t2.setCategory(groceriesCat);
            transactions.add(t2);

            // transaction 3
            Transaction t3 = getNewTransaction(u.getId(), -1500, "H AB");
            t3.setCategory(groceriesCat);
            transactions.add(t3);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            Transaction transaction = getNewTransaction(u.getId(), -100, "ab");
            transaction.setCategory(groceriesCat);
            transactionDao.saveAndIndex(u, Collections.singletonList(transaction), true);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());

            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsStopWordTest2() {
        List<User> users = getTestUsers("similarTransactionsStopWordTest2");

        users.forEach(u ->{
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "H AB");
            t1.setCategory(groceriesCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "ab");
            transaction.setCategory(groceriesCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());

            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsStopCities() {
        List<User> users = getTestUsers("similarTransactionsStopCities");

        users.forEach(u-> {
            CategoryRepository categoryRepo = serviceContext.getRepository(CategoryRepository.class);
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);

            Category hobbyCat = categoryRepo.findByCode(categoryConfiguration.getVacationCode());

            // Populate index.

            List<Transaction> transactions = Lists.newArrayList();

            Transaction t1 = getNewTransaction(u.getId(), -1500, "Linkopings Golf");
            t1.setCategory(hobbyCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;

            // Sleep to have time to index the transactions.

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "Linkopings Inneb");
            transaction.setCategory(hobbyCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());

            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsDontReturnYourselfTest() {
        List<User> users = getTestUsers("similarTransactionsDontReturnYourselfTest");

        users.forEach(u ->{
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "ICA");
            t1.setCategory(groceriesCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t1, u.getId(), t1.getCategoryId());

            // only one transaction, should not return yourself
            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void similarTransactionsFromUncategorized() {
        List<User> users = getTestUsers("similarTransactionsFromUncategorized");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "Mama Ye S Sushi");
            t1.setCategory(uncategorizedCat);
            transactions.add(t1);

            // transaction
            Transaction t2 = getNewTransaction(u.getId(), -8765, "Söders Cykel & S");
            t2.setCategory(uncategorizedCat);
            transactions.add(t2);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());

            // only one transaction, should not return yourself
            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void findSimilarTransactionsMaxResultLevelTest() {
        List<User> users = getTestUsers("findSimilarTransactionsMaxResultLevelTest");
        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();
            for (int i = 0; i < 15; i++) {
                Transaction t = getNewTransaction(u.getId(), -2000, "Coop");
                t.setCategory(groceriesCat);
                transactions.add(t);
            }
            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));

            Transaction transaction = getNewTransaction(u.getId(), -100, "Coop");
            transaction.setCategory(groceriesCat);

            List<Transaction> similarTransactions = searcher.findSimilarTransactions(transaction, u.getId(),
                    transaction.getCategoryId());
            Assert.assertEquals(15, similarTransactions.size());
        });
    }

    @Test
    public void testBgStopWords() {
        List<User> users = getTestUsers("testBgStopWords");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "BG 5061-0229, BORÅS ELNÄT AB, Ref 1033156108");
            t1.setCategory(groceriesCat);
            transactions.add(t1);

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -1500, "BG 5707-9055, TELE2 MOBIL, Ref 21354787919");
            t2.setCategory(groceriesCat);
            transactions.add(t2);

            // transaction 3
            Transaction t3 = getNewTransaction(u.getId(), -1500, "BG 485-3024, STRYKFRITT AB, Ref 120667");
            t3.setCategory(groceriesCat);
            transactions.add(t3);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t1, u.getId(), t1.getCategoryId());

            log.info("Similat to: " + t1.getOriginalDescription());
            for (Transaction t : similarTransactions) {
                log.info("\t" + t.getOriginalDescription());
            }

            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void testAutogiroStopWords() {
        List<User> users = getTestUsers("testAutogiroStopWords");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "Autogiro Världsnatur");
            t1.setCategory(groceriesCat);
            transactions.add(t1);

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -1500, "Autogiro Akademikerfö");
            t2.setCategory(groceriesCat);
            transactions.add(t2);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            // transaction 3
            Transaction t3 = getNewTransaction(u.getId(), -1500, "Autogiro Netflix");
            t3.setCategory(groceriesCat);

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t3, u.getId(), t3.getCategoryId());

            log.info("Similat to: " + t3.getOriginalDescription());
            for (Transaction t : similarTransactions) {
                log.info("\t" + t.getOriginalDescription());
            }

            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void testBgStopWordsPositive() {
        List<User> users = getTestUsers("testBgStopWordsPositive");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "BG 5707-9055, Ref 1033156108");
            t1.setCategory(groceriesCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;
            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -1500, "BG 5707-9055, Ref 21354787919");
            t2.setCategory(groceriesCat);

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());

            log.info("Similat to: " + t1.getOriginalDescription());
            for (Transaction t : similarTransactions) {
                log.info("\t" + t.getOriginalDescription());
            }

            Assert.assertEquals(1, similarTransactions.size());
        });
    }

    @Test
    public void testStopNumberWords() {
        List<User> users = getTestUsers("testStopNumberWords");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "Överföring 5392 33 154 65");
            t1.setCategory(groceriesCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -1500, "Kortuttag 250010 154 897328");
            t2.setCategory(groceriesCat);

            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());

            log.info("Similat to: " + t1.getOriginalDescription());
            for (Transaction t : similarTransactions) {
                log.info("\t" + t.getOriginalDescription());
            }

            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void testAmount() {
        List<User> users = getTestUsers("testAmount");
        users.forEach( u -> {

            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "ICA BANKEN AB");
            t1.setCategory(transfersCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), 1500, "ICA BANKEN AB");
            t2.setCategory(transfersCat);

            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());

            log.info("Similat to: " + t1.getOriginalDescription());
            for (Transaction t : similarTransactions) {
                log.info("\t" + t.getOriginalDescription());
            }

            Assert.assertEquals(0, similarTransactions.size());
        });
    }

    @Test
    public void testAmountPositive() {
        List<User> users = getTestUsers("testAmountPositivee");

        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), -1500, "ICA BANKEN AB");
            t1.setCategory(transfersCat);
            transactions.add(t1);

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), 1500, "ICA BANKEN AB");
            transactions.add(t2);
            t2.setCategory(transfersCat);

            transactionDao.saveAndIndex(u, transactions, true);;

            // transaction 3
            Transaction t3 = getNewTransaction(u.getId(), 1500, "ICA BANKEN AB");
            t3.setCategory(transfersCat);

            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t3, u.getId(), t2.getCategoryId());

            log.info("Similat to: " + t1.getOriginalDescription());
            for (Transaction t : similarTransactions) {
                log.info("\t" + t.getOriginalDescription());
            }

            Assert.assertEquals(1, similarTransactions.size());
        });

    }

    @Test
    public void testAmountNegative() {
        List<User> users = getTestUsers("testAmountNegative");
        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), 1500, "ICA BANKEN AB");
            t1.setCategory(transfersCat);
            transactions.add(t1);

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), -1500, "ICA BANKEN AB");
            transactions.add(t2);
            t2.setCategory(transfersCat);

            transactionDao.saveAndIndex(u, transactions, true);;

            // transaction 3
            Transaction t3 = getNewTransaction(u.getId(), -1500, "ICA BANKEN AB");
            t3.setCategory(transfersCat);

            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t3, u.getId(), t2.getCategoryId());

            log.info("Similat to: " + t1.getOriginalDescription());
            for (Transaction t : similarTransactions) {
                log.info("\t" + t.getOriginalDescription());
            }

            Assert.assertEquals(1, similarTransactions.size());
        });
    }

    @Test
    public void testZeroAmount() {
        List<User> users = getTestUsers("testZeroAmount");
        users.forEach(u -> {
            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), 0, "ICA BANKEN AB");
            t1.setCategory(transfersCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), 0, "ICA BANKEN AB");
            t2.setCategory(transfersCat);

            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());

            Assert.assertEquals(1, similarTransactions.size());
        });
    }

    @Test
    public void testAmountAndYear() {
        List<User> users = getTestUsers("testAmountAndYear");
        users.forEach(u -> {

            // populate index
            TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
            List<Transaction> transactions = Lists.newArrayList();

            // transaction 1
            Transaction t1 = getNewTransaction(u.getId(), 0, "ICA BANKEN AB");
            t1.setAmount(2301.);
            t1.setOriginalAmount(2301);
            t1.setCategory(transfersCat);
            transactions.add(t1);

            transactionDao.saveAndIndex(u, transactions, true);;

            // transaction 2
            Transaction t2 = getNewTransaction(u.getId(), 0, "ICA BANKEN AB");
            t2.setAmount(2509.);
            t2.setOriginalAmount(2509);
            t2.setCategory(transfersCat);

            // sleep to have time to index the transactions
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            SimilarTransactionsSearcher searcher = new SimilarTransactionsSearcher(serviceContext.getSearchClient(),
                    serviceContext.getRepository(AccountRepository.class),
                    serviceContext.getRepository(PostalCodeAreaRepository.class),
                    serviceContext.getRepository(CategoryRepository.class));
            List<Transaction> similarTransactions = searcher.findSimilarTransactions(t2, u.getId(), t2.getCategoryId());

            Assert.assertEquals(1, similarTransactions.size());
        });
    }

}
