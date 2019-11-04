package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface UkOpenBankingAisConfig {

    URL getBulkAccountRequestURL();

    URL getAccountBalanceRequestURL(String accountId);

    <T extends AccountPermissionResponse> String getIntentId(T accountPermissionResponse);

    URL createConsentRequestURL();

    <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType();

    String getInitialTransactionsPaginationKey(String accountId);

    URL getUpcomingTransactionRequestURL(String accountId);

    URL getApiBaseURL();

    public URL getWellKnownURL();

    public URL getIdentityDataURL();

    public URL getAppToAppURL();

    public List<String> getAdditionalPermissions();

    void setIdentityData(IdentityDataEntity identityData);

    public IdentityDataEntity getIdentityData();

    void setHolderName(String holderName);

    public String getHolderName();
}
