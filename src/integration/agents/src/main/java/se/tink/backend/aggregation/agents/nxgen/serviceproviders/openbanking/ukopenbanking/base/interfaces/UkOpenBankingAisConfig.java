package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface UkOpenBankingAisConfig {

    URL getBulkAccountRequestURL();

    URL getAccountBalanceRequestURL(String accountId);

    URL getAccountBeneficiariesRequestURL(String accountId);

    <T extends AccountPermissionResponse> String getIntentId(T accountPermissionResponse);

    URL createConsentRequestURL();

    <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType();

    String getInitialTransactionsPaginationKey(String accountId);

    URL getUpcomingTransactionRequestURL(String accountId);

    URL getApiBaseURL();

    URL getWellKnownURL();

    boolean isPartyEndpointEnabled();

    boolean isAccountPartiesEndpointEnabled();

    boolean isAccountPartyEndpointEnabled();

    URL getAppToAppURL();

    List<String> getAdditionalPermissions();

    void setIdentityData(IdentityDataEntity identityData);

    IdentityDataEntity getIdentityData();

    void setHolderName(String holderName);

    String getHolderName();

    AccountOwnershipType getAllowedAccountOwnershipType();

    String getOrganisationId();
}
