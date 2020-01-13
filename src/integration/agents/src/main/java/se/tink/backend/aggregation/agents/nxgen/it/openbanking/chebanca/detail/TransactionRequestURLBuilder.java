package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.Urls;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class TransactionRequestURLBuilder {

    public static URL buildTransactionRequestUrl(
            String customerId,
            String accountId,
            Date fromDate,
            Date toDate,
            Long nextAccountingIdx,
            Long nextNotAccountingIdx) {
        return new Builder(Urls.TRANSACTIONS)
                .withUrlParam(IdTags.CUSTOMER_ID, customerId)
                .withUrlParam(IdTags.PRODUCT_ID, accountId)
                .withUrlQueryParam(
                        QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DD_MM_YYYY.format(fromDate))
                .withUrlQueryParam(
                        QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DD_MM_YYYY.format(toDate))
                .withUrlQueryParam(IdTags.NEXT_ACCOUNTING_ID, getIndex(nextAccountingIdx))
                .withUrlQueryParam(IdTags.NEXT_NOT_ACCOUNTING_ID, getIndex(nextNotAccountingIdx))
                .build();
    }

    private static String getIndex(Long nextIdx) {
        if (nextIdx == null || nextIdx == 0) {
            return null;
        }
        return nextIdx.toString();
    }

    private static class Builder {
        private URL url;

        Builder(URL baseUrl) {
            url = baseUrl;
        }

        Builder withUrlParam(String paramName, String paramValue) {
            url = url.parameter(paramName, paramValue);
            return this;
        }

        Builder withUrlQueryParam(String paramName, String paramValue) {
            url = url.queryParam(paramName, paramValue);
            return this;
        }

        URL build() {
            return url;
        }
    }
}
