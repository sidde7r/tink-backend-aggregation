package se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit.authenticator.entity.UnicreditConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit.authenticator.rpc.UnicreditConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class UnicreditApiClient extends UnicreditBaseApiClient {

    public UnicreditApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean requestIsManual) {
        super(client, persistentStorage, credentials, requestIsManual);
    }

    @Override
    protected ConsentRequest getConsentRequest() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Today's date
        c.add(Calendar.DATE, 1); // Adds 1 day

        return new UnicreditConsentRequest(
                new UnicreditConsentAccessEntity(FormValues.ALL_ACCOUNTS),
                true,
                ThreadSafeDateFormat.FORMATTER_DAILY.format(c.getTime()),
                FormValues.FREQUENCY_PER_DAY,
                false);
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return UnicreditConsentResponse.class;
    }

    @Override
    protected URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(consentResponse.getScaRedirect());
    }

    @Override
    protected String getTransactionsDateFrom() {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                DateUtils.addDays(new Date(), -QueryValues.MAX_PERIOD_IN_DAYS));
    }
}
