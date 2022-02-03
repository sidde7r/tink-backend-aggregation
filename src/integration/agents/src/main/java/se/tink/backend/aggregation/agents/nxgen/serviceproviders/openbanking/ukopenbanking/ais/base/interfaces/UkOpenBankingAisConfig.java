package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface UkOpenBankingAisConfig {

    URL getBulkAccountRequestURL();

    URL getAccountBalanceRequestURL(String accountId);

    URL getAccountBeneficiariesRequestURL(String accountId);

    URL createConsentRequestURL();

    URL getConsentDetailsRequestURL(String consentId);

    String getInitialTransactionsPaginationKey(String accountId);

    URL getUpcomingTransactionRequestURL(String accountId);

    URL getApiBaseURL();

    URL getWellKnownURL();

    boolean isPartyEndpointEnabled();

    boolean isAccountPartiesEndpointEnabled();

    boolean isAccountPartyEndpointEnabled();

    boolean isFetchingTransactionsFromTheNewestToTheOldest();

    ImmutableSet<UkObScope> getAvailablePermissions();

    String getOrganisationId();
}
