package se.tink.backend.system.cli.migration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.utils.CassandraTransactionConverter;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AffectedTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(AffectedTransactionsCommand.class);

    public AffectedTransactionsCommand() {
        super("copy-transactions",
                "copy transaction for affected user");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
                       ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        String userTransactionsFileName = System.getProperty("userTransactionsFile");
        Preconditions.checkArgument(userTransactionsFileName != null,
                "'Must specify file with user transactions.");
        File userTransactionsFile = new File(userTransactionsFileName);
        List<Transaction> transactionList = StreamSupport.stream(Spliterators.spliteratorUnknownSize(CSVParser
                .parse(userTransactionsFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT)
                .iterator(), 0), false)
                .map(this::transformRow)
                .map(CassandraTransactionConverter::fromCassandraTransaction)
                .collect(Collectors.toList());

        TransactionDao indexedTransactionDAO = serviceContext.getDao(TransactionDao.class);
        Map<String, List<Transaction>> userIdTransaction = transactionList.stream()
                .collect(Collectors.groupingBy(Transaction::getUserId));
        userIdTransaction.forEach((userId, transactions) -> {
            Lists.partition(transactionList, 100)
                    .forEach(batch -> {
                        try {
                            Thread.sleep(100L);
                            indexedTransactionDAO.saveAndIndex(userId, batch, true);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                        }
                    });
        });
        log.info("transactions copied {}", transactionList.size());
    }

    private CassandraTransaction transformRow(CSVRecord transactionRow) {
        CassandraTransaction cassandraTransaction = new CassandraTransaction();
        cassandraTransaction.setUserModifiedAmount(nullOrString(transactionRow.get(0)) == null ? false : Boolean.valueOf(transactionRow.get(0)));
        cassandraTransaction.setUserModifiedCategory(nullOrString(transactionRow.get(1)) == null ? false : Boolean.valueOf(transactionRow.get(1)));
        cassandraTransaction.setId(UUID.fromString(transactionRow.get(2)));
        cassandraTransaction.setCredentialsId(UUID.fromString(transactionRow.get(3)));
        cassandraTransaction.setInternalPayloadSerialized((transactionRow.get(4)));
        cassandraTransaction.setCategoryType(nullOrString(transactionRow.get(5)) == null ? null : CategoryTypes.valueOf(transactionRow.get(5)));
        cassandraTransaction.setOriginalDate(tryParseDate(transactionRow.get(6)));
        cassandraTransaction.setOriginalDescription(nullOrString(transactionRow.get(7)));
        cassandraTransaction.setUserModifiedLocation(nullOrString(transactionRow.get(8)) == null ? false : Boolean.valueOf(transactionRow.get(8)));
        cassandraTransaction.setPending(nullOrString(transactionRow.get(9)) == null ? false : Boolean.valueOf(transactionRow.get(9)));
        cassandraTransaction.setNotes(nullOrString(transactionRow.get(10)));
        cassandraTransaction.setUserId(UUID.fromString(transactionRow.get(11)));
        cassandraTransaction.setAmount(new BigDecimal(transactionRow.get(12)));
        cassandraTransaction.setInserted(Long.parseLong(transactionRow.get(13)));
        cassandraTransaction.setDescription(nullOrString(transactionRow.get(14)));
        cassandraTransaction.setCategoryId(UUID.fromString(transactionRow.get(15)));
        cassandraTransaction.setFormattedDescription(nullOrString(transactionRow.get(16)));
        cassandraTransaction.setAccountId(UUID.fromString(transactionRow.get(17)));
        cassandraTransaction.setDate(tryParseDate(transactionRow.get(18)));
        cassandraTransaction.setLastModified(nullOrString(transactionRow.get(19)) == null ? tryParseDate(transactionRow.get(18)) : tryParseDate(transactionRow.get(19)));
        cassandraTransaction.setMerchantId((nullOrString(transactionRow.get(20)) == null) ? null : UUID.fromString(transactionRow.get(20)));
        cassandraTransaction.setPayloadSerialized(nullOrString(transactionRow.get(21)));
        cassandraTransaction.setUserModifiedDate(nullOrString(transactionRow.get(22)) == null ? false : Boolean.valueOf(transactionRow.get(22)));
        cassandraTransaction.setTimestamp(Long.parseLong(transactionRow.get(23)));
        cassandraTransaction.setType(nullOrString(transactionRow.get(24)) == null ? TransactionTypes.DEFAULT : TransactionTypes.valueOf(transactionRow.get(24)));
        cassandraTransaction.setOriginalAmount(new BigDecimal(transactionRow.get(25)));
        cassandraTransaction.setUserModifiedDescription(nullOrString(transactionRow.get(26)) == null ? false : Boolean.valueOf(transactionRow.get(26)));
        return cassandraTransaction;
    }

    private String nullOrString(String whatIsThis) {
        if (whatIsThis == null || whatIsThis.isEmpty() || whatIsThis.equals("null")) {
            return null;
        }

        return whatIsThis;
    }

    private Date tryParseDate(String dateString) {
        try {
            return ThreadSafeDateFormat.FORMATTER_MINS_WITH_TIMEZONE.parse(dateString);
        } catch (ParseException e) {
            log.warn("Date parsing failed: {}", dateString, e);
            return null;
        }
    }

}

