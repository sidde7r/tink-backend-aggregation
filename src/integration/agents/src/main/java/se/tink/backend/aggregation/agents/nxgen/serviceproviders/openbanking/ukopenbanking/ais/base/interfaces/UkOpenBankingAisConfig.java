package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface UkOpenBankingAisConfig {

    URL getBulkAccountRequestURL();

    URL getAccountBalanceRequestURL(String accountId);

    URL getAccountBeneficiariesRequestURL(String accountId);

    <T extends AccountPermissionResponse> String getIntentId(T accountPermissionResponse);

    URL createConsentRequestURL();

    URL getConsentDetailsRequestURL(String consentId);

    <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType();

    String getInitialTransactionsPaginationKey(String accountId);

    URL getUpcomingTransactionRequestURL(String accountId);

    URL getApiBaseURL();

    URL getWellKnownURL();

    boolean isPartyEndpointEnabled();

    boolean isAccountPartiesEndpointEnabled();

    boolean isAccountPartyEndpointEnabled();

    ImmutableSet<String> getPermissions();

    Set<AccountOwnershipType> getAllowedAccountOwnershipTypes();

    String getOrganisationId();
}
