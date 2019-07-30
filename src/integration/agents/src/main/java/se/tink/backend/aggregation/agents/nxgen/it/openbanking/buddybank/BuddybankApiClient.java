package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import java.util.Calendar;
import java.util.Date;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.entity.BuddybankConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class BuddybankApiClient extends UnicreditBaseApiClient {

    public BuddybankApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        super(client, persistentStorage, credentials);
    }

    @Override
    protected ConsentRequest getConsentRequest() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Today's date
        c.add(Calendar.DATE, 1); // Adds 1 day

        return new BuddybankConsentRequest(
                new BuddybankConsentAccessEntity(FormValues.ALL_ACCOUNTS),
                true,
                ThreadSafeDateFormat.FORMATTER_DAILY.format(c.getTime()),
                4,
                false);
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return BuddybankConsentResponse.class;
    }

    @Override
    protected URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(getConfiguration().getBaseUrl() + consentResponse.getScaRedirect());
    }
}
