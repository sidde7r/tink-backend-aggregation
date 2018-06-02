package se.tink.backend.system.cli.extraction;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.api.TransactionService;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionQuery;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class ExtractDemoUsersCommand extends ServiceContextCommand<ServiceConfiguration> {
    public ExtractDemoUsersCommand() {
        super("extract-demo-users", "Extracts all users from database into the demo format");
    }

    private static final String BASE_PATH = System.getProperty("user.home") + File.separator + "demo";
    private static final String DEFAULT_CHARSET = "CP1252";
    private static final LogUtils log = new LogUtils(ExtractDemoUsersCommand.class);

    private static HashMap<String, String> categoryMap = new HashMap<String, String>();

    protected static WorkbookSettings createSettings() {
        WorkbookSettings ws = new WorkbookSettings();
        ws.setEncoding(DEFAULT_CHARSET);
        return ws;
    }

    private static void extractTransactions(String testUserName, String testAccountName, String testAccountBankId,
            List<Transaction> transactions) throws IOException, RowsExceededException, WriteException {
        File accountFile = new File(BASE_PATH + File.separator + testUserName + File.separator + testAccountBankId
                + ".xls");

        WritableWorkbook accountWorkbook = Workbook.createWorkbook(accountFile, createSettings());
        accountWorkbook.createSheet("Transactions", 0);

        WritableSheet accountSheet = accountWorkbook.getSheet(0);

        accountSheet.addCell(new Label(0, 0, "date"));
        accountSheet.addCell(new Label(1, 0, "description"));
        accountSheet.addCell(new Label(2, 0, "category"));
        accountSheet.addCell(new Label(3, 0, "amount"));
        accountSheet.addCell(new Label(4, 0, "type"));
        accountSheet.addCell(new Label(5, 0, "pending"));
        accountSheet.addCell(new Label(6, 0, "payload"));

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);

            accountSheet.addCell(new Label(0, i + 1, ThreadSafeDateFormat.FORMATTER_DAILY.format(transaction.getDate())));
            accountSheet.addCell(new Label(1, i + 1, transaction.getOriginalDescription()));
            accountSheet.addCell(new Label(2, i + 1, categoryMap.get(transaction.getCategoryId())));
            accountSheet.addCell(new Label(3, i + 1, Double.toString(transaction.getAmount())));

            if (transaction.getType() != null && transaction.getType() != TransactionTypes.DEFAULT) {
                accountSheet.addCell(new Label(4, i + 1, transaction.getType().toString()));
            }

            if (transaction.isPending()) {
                accountSheet.addCell(new Label(5, i + 1, "true"));
            }

            if (transaction.getPayload() != null) {
                List<TransactionPayloadTypes> payloadKeys = Lists.newArrayList(transaction.getPayload().keySet());

                for (int j = 0; j < payloadKeys.size(); j++) {
                    accountSheet.addCell(new Label(6 + (j * 2), i + 1, payloadKeys.get(j).toString()));
                    accountSheet
                            .addCell(new Label(7 + (j * 2), i + 1, transaction.getPayloadValue(payloadKeys.get(j))));
                }
            }
        }

        accountWorkbook.write();
        accountWorkbook.close();
    }

    private void extractUser(String testUserName, User user, AccountRepository accountRepository,
            TransactionService transactionService) throws Exception {
        File directory = new File(BASE_PATH + File.separator + testUserName);

        try {
            directory.delete();
        } catch (Exception e) {
        }

        directory.mkdirs();

        log.info("Extracting user: " + testUserName + " (actually " + user.getUsername() + ")...");

        File accountFile = new File(directory + File.separator + "accounts.xls");

        WritableWorkbook accountsWorkbook = Workbook.createWorkbook(accountFile, createSettings());
        accountsWorkbook.createSheet("Accounts", 0);

        WritableSheet accountsSheet = accountsWorkbook.getSheet(0);

        List<Account> accounts = accountRepository.findByUserId(user.getId());

        accountsSheet.addCell(new Label(0, 0, "bankId"));
        accountsSheet.addCell(new Label(1, 0, "name"));
        accountsSheet.addCell(new Label(2, 0, "type"));
        accountsSheet.addCell(new Label(3, 0, "currency"));
        accountsSheet.addCell(new Label(4, 0, "balance"));

        for (int j = 0; j < accounts.size(); j++) {
            Account account = accounts.get(j);

            String testAccountName = "Konto " + (j + 1);
            String testAccountBankId = "konto" + (j + 1);

            accountsSheet.addCell(new Label(0, j + 1, testAccountBankId));
            accountsSheet.addCell(new Label(1, j + 1, testAccountName));
            accountsSheet.addCell(new Label(2, j + 1, account.getType().toString()));
            accountsSheet.addCell(new Label(3, j + 1, "SEK"));
            accountsSheet.addCell(new Label(4, j + 1, Double.toString(account.getBalance())));

            log.info("\tProcessing account: " + testAccountName + " (" + account.getType() + ")...");
            
            TransactionQuery transactionQuery = new TransactionQuery();
            transactionQuery.setAccounts(Lists.newArrayList(account.getId()));
            
            extractTransactions(testUserName, testAccountName, testAccountBankId, 
                    transactionService.query(user, transactionQuery).getTransactions());
        }

        accountsWorkbook.write();
        accountsWorkbook.close();
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        CategoryRepository categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);
        TransactionService transactionService = serviceContext.getServiceFactory().getTransactionService();

        List<User> users = userRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        for (Category cat : categories) {
            categoryMap.put(cat.getId(), cat.getCode());
        }

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            String testUserName = "test" + (i + 1);

            try {
                extractUser(testUserName, user, accountRepository, transactionService);
            } catch (Exception e) {
                log.error(user.getId(), "Could not extract user");
            }
            log.info("Done extracting user data.");
        }

    }
}
