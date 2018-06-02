package se.tink.backend.system.cli;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.nio.charset.Charset;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.core.Transaction;

public class TransactionEncodingCheckerCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static enum WriteOperations {
        DESCRIPTION,
        FORMATTTED_DESCRIPTION,
        ORIGINAL_DESCRIPTION
    }

    // http://stackoverflow.com/a/140861/260805
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public TransactionEncodingCheckerCommand() {
        super("transaction-encoding-test", "Test reading and writing various encodings to and from DB.");
    }

    private void detailedPrint(String what, String string) {
        System.out.println("=========== READ ==========");
        System.out.println(what + ": " + string);
        System.out.println("--------------------------");
        System.out.println("utf16:");
        printByteArray(string.getBytes(Charset.forName("utf16")));
        System.out.println("utf8:");
        printByteArray(string.getBytes(Charset.forName("utf8")));
        System.out.println("==========================");
    }

    private void printByteArray(byte[] weirdSalary) {
        for (byte b : weirdSalary) {
            // http://stackoverflow.com/a/2817883/260805
            System.out.println(" - " + String.format("%02X", b));
        }
    }

    private void read(TransactionDao transactionDao, String userId, String transactionId) {
        Transaction transaction = transactionDao.findOneByUserIdAndId(userId, transactionId, Optional.empty());
        detailedPrint("description", transaction.getDescription());
        detailedPrint("originaldescription", transaction.getOriginalDescription());
        detailedPrint("formatteddescription", transaction.getFormattedDescription());
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        String transactionId = System.getProperty("transactionId");
        String userId = System.getProperty("userId");
        Preconditions.checkNotNull(transactionId);

        TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);

        String writeOperationProperty = System.getProperty("writeOperation");
        if (writeOperationProperty != null) {
            System.out.println("READ BEFORE:");
            read(transactionDao, userId, transactionId);
            
            WriteOperations writeOperation = WriteOperations.valueOf(writeOperationProperty);

            String inputEncoding = System.getProperty("inputEncoding", "utf16");
            String toWrite = System.getProperty("toWrite");
            byte[] bytesToWrite = hexStringToByteArray(toWrite);
            String stringToWrite = new String(bytesToWrite, Charset.forName(inputEncoding));

            write(transactionDao, userId, transactionId, writeOperation, stringToWrite);

            System.out.println("READ AFTER:");
            read(transactionDao, userId, transactionId);
        } else {
            System.out.println("Not writing, only reading:");
            read(transactionDao, userId, transactionId);
        }

    }

    private void write(TransactionDao transactionDao, String userId,
            String transactionId, WriteOperations writeOperation, String stringToWrite) {
        Transaction transaction = transactionDao.findOneByUserIdAndId(userId, transactionId, Optional.empty());

        if (writeOperation.equals(WriteOperations.DESCRIPTION)) {
            transaction.setDescription(stringToWrite);
        } else if (writeOperation.equals(WriteOperations.FORMATTTED_DESCRIPTION)) {
            transaction.setFormattedDescription(stringToWrite);
        } else if (writeOperation.equals(WriteOperations.ORIGINAL_DESCRIPTION)) {
            transaction.setOriginalDescription(stringToWrite);
        }
        System.out.println("Done updating the transaction.");

        transactionDao.save(userId, transaction);
    }

}
