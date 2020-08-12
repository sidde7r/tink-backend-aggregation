package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc;

import java.util.Date;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.utils.BnpParibasFormatUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class TransactionalAccountTransactionsRequest {
    private String triAV;
    private String startDate;
    private String endDate;
    private String ibanCrypte;
    private String pastOrPending;

    public static TransactionalAccountTransactionsRequest create(
            String triAV, Date fromDate, Date toDate, String ibanKey, String pastOrPending) {
        String startDate = BnpParibasFormatUtils.TRANSACTION_DATE_FORMATTER.format(fromDate);
        String endDate = BnpParibasFormatUtils.TRANSACTION_DATE_FORMATTER.format(toDate);

        return new TransactionalAccountTransactionsRequest(
                triAV, startDate, endDate, ibanKey, pastOrPending);
    }
}
