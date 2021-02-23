package se.tink.backend.aggregation.agents.utils.demo;

import static java.time.temporal.TemporalAdjusters.next;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.rpc.Account;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.demo.DemoCredentials;
import se.tink.libraries.credentials.rpc.Credentials;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.enums.SwedishGiroType;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public class DemoDataUtils {

    private static final Splitter SPLITTER = Splitter.on('\t');
    private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;
    private static final int PENDING_FUTURE_TRANSACTION_CUTOFF = 31;
    private static final int MIN_DATA_SIZE = 3;
    private static final String FILENAME_ENDS_FOR_UNCHANGED_DATE = "ud";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<Account> readAccounts(File file, Credentials credentials)
            throws IOException {
        List<String> lines = Files.readLines(file, DEFAULT_CHARSET);

        List<Account> accounts = Lists.newArrayList();

        for (int i = 1; i < lines.size(); i++) {
            List<String> data = Lists.newArrayList(SPLITTER.split(lines.get(i)));

            Account account = new Account();

            // Generally we always validate bankid string here to make sure it isn't reformatted by
            // a bank. However,
            // since this is useful in testing and demo we can be a little sloppy.

            account.setAccountNumber(data.get(0));
            account.setBankId(data.get(0));
            account.putIdentifier(new SwedishIdentifier(data.get(0)));
            account.setName(data.get(1));
            account.setType(se.tink.libraries.account.enums.AccountTypes.valueOf(data.get(2)));
            account.setBalance(StringUtils.parseAmount(data.get(4).replace("\"", "")));
            account.setCredentialsId(credentials.getId());
            account.setUserId(credentials.getUserId());

            accounts.add(account);
        }

        return accounts;
    }

    public static List<se.tink.backend.agents.rpc.Account> readAggregationAccounts(
            File file, se.tink.backend.agents.rpc.Credentials credentials) throws IOException {
        return readAccounts(file, CoreCredentialsMapper.fromAggregationCredentials(credentials))
                .stream()
                .map(CoreAccountMapper::toAggregation)
                .collect(Collectors.toList());
    }

    public static List<Transaction> readTransactionsWithRandomization(
            DemoCredentials demoCredentials,
            File file,
            se.tink.backend.agents.rpc.Account account,
            int transactionsToRandomize)
            throws IOException {

        List<Transaction> transactions = readTransactions(demoCredentials, file, account);

        Ordering<Transaction> orderingOnDate = Ordering.natural().onResultOf(Transaction::getDate);

        List<Transaction> lastTransactions =
                orderingOnDate.greatestOf(transactions, transactionsToRandomize);

        for (Transaction t : lastTransactions) {
            // Add some dummy randomization
            t.setAmount((double) Math.round(t.getAmount() * Math.random()));
        }

        return transactions;
    }

    public static List<Transaction> readTransactions(
            DemoCredentials demoCredentials, File file, se.tink.backend.agents.rpc.Account account)
            throws IOException {
        return readTransactions(demoCredentials, file, account, false);
    }

    public static List<Transaction> readTransactions(
            DemoCredentials demoCredentials,
            File file,
            se.tink.backend.agents.rpc.Account account,
            boolean skipFutureTransactions)
            throws IOException {

        Date today = DateUtils.setInclusiveStartTime(new Date());
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(today);

        List<Transaction> transactions = Lists.newArrayList();

        // Transaction file is not needed for loans and investment
        if (account.getType() != null && Objects.equal(account.getType(), AccountTypes.LOAN)
                || account.getType() != null
                        && Objects.equal(account.getType(), AccountTypes.INVESTMENT)) {
            return transactions;
        }

        List<String> lines = Files.readLines(file, DEFAULT_CHARSET);

        for (int i = 1; i < lines.size(); i++) {
            List<String> data = Lists.newArrayList(SPLITTER.split(lines.get(i)));

            if (data.size() < MIN_DATA_SIZE) {
                continue;
            }

            DateTime parsedDate = new DateTime(DateUtils.parseDate(data.get(0)));

            Date transDate;

            if (demoCredentials != null
                    || file.getParentFile().getName().endsWith(FILENAME_ENDS_FOR_UNCHANGED_DATE)) {
                transDate = parsedDate.toDate();
            } else {
                int year = todayCal.get(Calendar.YEAR);
                transDate = parsedDate.withYear(year).toDate();
                if (transDate.getTime() > today.getTime()) {
                    transDate = parsedDate.withYear(year - 1).toDate();
                }
            }

            Transaction transaction = new Transaction();

            transaction.setDate(DateUtils.flattenTime(transDate));
            transaction.setDescription(data.get(1));
            transaction.setAmount(StringUtils.parseAmount(data.get(2)));
            transaction.setAccountId(account.getId());
            transaction.setUserId(account.getUserId());
            transaction.setCredentialsId(account.getCredentialsId());

            String transactionType = null;

            if (data.size() > 3) {
                transactionType = data.get(3);
            }

            if (transactionType != null && transactionType.trim().length() > 0) {
                transaction.setType(TransactionTypes.valueOf(transactionType));
            } else {
                transaction.setType(TransactionTypes.DEFAULT);
            }

            String pending = "";
            if (data.size() > 4) {
                pending = data.get(4);
            }

            if (pending != null && pending.trim().length() > 0 && "true".equals(pending.trim())) {
                transaction.setPending(true);
            }

            if (transDate.after(today)) {
                // Skip all future transactions if requested.
                if (skipFutureTransactions) {
                    continue;
                }

                // Skip all non-pending future transactions per default.
                if (transaction.getType() != TransactionTypes.PAYMENT
                        || DateUtils.daysBetween(today, transDate)
                                >= PENDING_FUTURE_TRANSACTION_CUTOFF) {
                    continue;
                } else {
                    transaction.setPending(true);
                    if (transaction.getDescription().equals("Hyra Stockholm")) {

                        Transfer fakePayment =
                                createFakeTransfer(
                                        "HSB Stockholm Ek",
                                        "1157002203",
                                        1954,
                                        "3300308",
                                        SwedishGiroType.BG,
                                        7,
                                        TransferType.PAYMENT);

                        fakePayment.setId(UUIDUtils.fromTinkUUID(transaction.getId()));
                        fakePayment.setSource(account.getIdentifier(AccountIdentifier.Type.SE));

                        transaction.setPayload(
                                TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                                MAPPER.writeValueAsString(fakePayment));
                        transaction.setPayload(
                                TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                                UUIDUtils.toTinkUUID(fakePayment.getId()));
                    }
                }
            }

            for (int j = 0; j < 10; j++) {
                try {
                    if (data.size() < 6 + (j * 2)) {
                        break;
                    }

                    String payloadKey = data.get(5 + (j * 2));

                    if (payloadKey == null || payloadKey.trim().length() == 0) {
                        break;
                    }

                    transaction.setPayload(
                            TransactionPayloadTypes.valueOf(payloadKey), data.get(6 + (j * 2)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }

            transactions.add(transaction);
        }

        return transactions;
    }

    public static Transfer createFakeTransfer(
            String name,
            String ocr,
            double amount,
            String giroNumber,
            SwedishGiroType giroType,
            int dueInDays,
            TransferType transferType) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(ocr);
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(amount));
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage(name);
        transfer.setType(transferType);
        transfer.setDestination(
                AccountIdentifier.create(giroType.toAccountIdentifierType(), giroNumber));
        transfer.setDueDate(org.apache.commons.lang3.time.DateUtils.addDays(new Date(), dueInDays));

        return transfer;
    }

    public static Transfer createFakeTransferInComingSunday(
            String name,
            String ocr,
            double amount,
            String giroNumber,
            SwedishGiroType giroType,
            TransferType transferType) {
        return createFakeTransfer(
                name,
                ocr,
                amount,
                giroNumber,
                giroType,
                daysToSunday(LocalDate.now()),
                transferType);
    }

    public static int daysToSunday(LocalDate date) {
        final LocalDate nextSunday = date.with(next(DayOfWeek.SUNDAY));
        return nextSunday.getDayOfYear() > date.getDayOfYear()
                ? nextSunday.getDayOfYear() - date.getDayOfYear()
                : nextSunday.getDayOfYear()
                        + Year.of(date.getYear()).length()
                        - date.getDayOfYear();
    }
}
