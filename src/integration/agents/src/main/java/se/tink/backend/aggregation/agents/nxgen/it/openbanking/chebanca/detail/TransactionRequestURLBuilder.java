package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class TransactionRequestURLBuilder {

    public static URL buildTransactionRequestUrl(
            String customerId, String accountId, Date fromDate, Date toDate) {
        return ChebancaConstants.Urls.TRANSACTIONS
                .parameter(ChebancaConstants.IdTags.CUSTOMER_ID, customerId)
                .parameter(ChebancaConstants.IdTags.PRODUCT_ID, accountId)
                .queryParam(
                        ChebancaConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DD_MM_YYYY.format(fromDate))
                .queryParam(
                        ChebancaConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DD_MM_YYYY.format(toDate));
    }
}
