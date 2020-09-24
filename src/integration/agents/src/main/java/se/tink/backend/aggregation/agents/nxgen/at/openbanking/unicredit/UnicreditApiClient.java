package se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit;

import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class UnicreditApiClient extends UnicreditBaseApiClient {

    UnicreditApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            Credentials credentials,
            boolean manualRequest,
            UnicreditProviderConfiguration providerConfiguration) {
        super(
                client,
                persistentStorage,
                sessionStorage,
                credentials,
                manualRequest,
                providerConfiguration);
    }

    @Override
    protected String getTransactionsDateFrom() {
        if (manualRequest) {
            return QueryValues.TRANSACTION_FROM_DATE;
        } else {
            return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                    DateUtils.addDays(new Date(), -QueryValues.MAX_PERIOD_IN_DAYS));
        }
    }
}
