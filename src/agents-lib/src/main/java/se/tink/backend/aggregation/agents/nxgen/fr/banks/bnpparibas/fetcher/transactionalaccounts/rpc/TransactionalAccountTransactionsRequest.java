package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.utils.BnpParibasFormatUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionalAccountTransactionsRequest {
    private String triAV;
    private String startDate;
    private String pastOrPending;
    private String ibanCrypte;
    private String endDate;

    private TransactionalAccountTransactionsRequest(String startDate, String endDate, String ibanKey) {
        this.triAV = BnpParibasConstants.TransactionalAccountTransactions.TRIAV;
        this.startDate = startDate;
        this.pastOrPending = BnpParibasConstants.TransactionalAccountTransactions.PAST_OR_PENDING;
        this.ibanCrypte = ibanKey;
        this.endDate = endDate;
    }

    public static TransactionalAccountTransactionsRequest create(Date fromDate, Date toDate,
            String ibanKey) {
        String startDate = BnpParibasFormatUtils.TRANSACTION_DATE_FORMATTER.format(fromDate);
        String endDate = BnpParibasFormatUtils.TRANSACTION_DATE_FORMATTER.format(toDate);

        return new TransactionalAccountTransactionsRequest(startDate, endDate, ibanKey);
    }
}
