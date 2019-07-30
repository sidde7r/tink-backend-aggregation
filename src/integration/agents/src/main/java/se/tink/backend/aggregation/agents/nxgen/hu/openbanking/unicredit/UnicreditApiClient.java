package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.entity.ConsentPayloadEntity;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.entity.UnicreditConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.rpc.UnicreditConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class UnicreditApiClient extends UnicreditBaseApiClient {

    public UnicreditApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        super(client, persistentStorage, credentials);
    }

    @Override
    protected ConsentRequest getConsentRequest() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Today's date
        c.add(Calendar.DATE, 1); // Adds 1 day

        return new UnicreditConsentRequest(
                new UnicreditConsentAccessEntity(
                        Collections.singletonList(
                                new ConsentPayloadEntity(
                                        getCredentials().getField(Key.LOGIN_INPUT)))),
                true,
                ThreadSafeDateFormat.FORMATTER_DAILY.format(c.getTime()),
                4,
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
}
