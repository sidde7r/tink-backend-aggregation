package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.parser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataTransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;

public class BankdataTransactionParser {

    public Transaction parseTransaction(BankdataTransactionEntity bankdataTransaction) {
        return Transaction.builder()
                .setDescription(bankdataTransaction.getText())
                .setDate(parseDate(bankdataTransaction.getTransactionDate()))
                .setAmount(Amount.inDKK(bankdataTransaction.getMainAmount()))
                .build();
    }

    public UpcomingTransaction parseUpcomingTransaction(
            BankdataTransactionEntity bankdataTransaction) {
        return UpcomingTransaction.builder()
                .setDescription(bankdataTransaction.getText())
                .setDate(parseDate(bankdataTransaction.getTransactionDate()))
                .setAmount(Amount.inDKK(bankdataTransaction.getMainAmount()))
                .build();
    }

    private Date parseDate(String transactionDate) {
        // how do we do this correctly?? Is there a plan?
        LocalDate localDate =
                LocalDate.parse(
                        transactionDate,
                        DateTimeFormatter.ofPattern(BankdataConstants.Fetcher.DATE_FORMAT));
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
