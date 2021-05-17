package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingAisConfiguration implements UkOpenBankingAisConfig {
    private final URL apiBaseURL;
    private final URL wellKnownURL;
    private final Set<PartyEndpoint> partyEndpoints;
    private final Set<AccountOwnershipType> allowedAccountOwnershipTypes;
    private final String organisationId;

    protected UkOpenBankingAisConfiguration(Builder builder) {
        this.apiBaseURL = builder.apiBaseURL;
        this.wellKnownURL = builder.wellKnownURL;
        this.partyEndpoints = builder.partyEndpoints;
        this.allowedAccountOwnershipTypes = builder.allowedAccountOwnershipTypes;
        this.organisationId = builder.organisationId;
    }

    @Override
    public String getIntentId(AccountPermissionResponse accountPermissionResponse) {
        AccountPermissionResponseV31 accountPermissionResponseV31 =
                (AccountPermissionResponseV31) accountPermissionResponse;
        return accountPermissionResponseV31.getData().getAccountConsentId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType() {
        return (Class<T>) AccountPermissionResponseV31.class;
    }

    @Override
    public URL getBulkAccountRequestURL() {
        return apiBaseURL.concat(ApiServices.ACCOUNT_BULK_REQUEST);
    }

    @Override
    public URL getAccountBalanceRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ApiServices.ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public URL getAccountBeneficiariesRequestURL(String accountId) {
        return apiBaseURL.concat("/accounts").concat("/" + accountId).concat("/beneficiaries");
    }

    @Override
    public URL createConsentRequestURL() {
        return apiBaseURL.concat("/account-access-consents");
    }

    @Override
    public URL getConsentDetailsRequestURL(String consentId) {
        return apiBaseURL.concat(
                String.format(
                        UkOpenBankingV31Constants.ApiServices.CONSENT_DETAILS_REQUEST, consentId));
    }

    @Override
    public URL getUpcomingTransactionRequestURL(String accountId) {
        return apiBaseURL.concat(
                String.format(ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }

    @Override
    public URL getApiBaseURL() {
        return this.apiBaseURL;
    }

    @Override
    public URL getWellKnownURL() {
        return this.wellKnownURL;
    }

    @Override
    public boolean isPartyEndpointEnabled() {
        return partyEndpoints.contains(PartyEndpoint.PARTY);
    }

    @Override
    public boolean isAccountPartiesEndpointEnabled() {
        return partyEndpoints.contains(PartyEndpoint.ACCOUNT_ID_PARTIES);
    }

    @Override
    public boolean isAccountPartyEndpointEnabled() {
        return partyEndpoints.contains(PartyEndpoint.ACCOUNT_ID_PARTY);
    }

    @Override
    public ImmutableSet<String> getPermissions() {

        Set<String> set = new HashSet<>();
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_ACCOUNTS_DETAIL.getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_BALANCES.getValue());
        set.add(
                OpenIdAuthenticatorConstants.ConsentPermission.READ_BENEFICIARIES_DETAIL
                        .getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_DIRECT_DEBITS.getValue());
        set.add(
                OpenIdAuthenticatorConstants.ConsentPermission.READ_STANDING_ORDERS_DETAIL
                        .getValue());
        set.add(
                OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_CREDITS
                        .getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue());

        if (isPartyEndpointEnabled()) {
            set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY_PSU.getValue());
        }

        if (isAccountPartiesEndpointEnabled() || isAccountPartyEndpointEnabled()) {
            set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY.getValue());
        }

        return ImmutableSet.<String>builder().addAll(set).build();
    }

    @Override
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ApiServices.ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    @Override
    public Set<AccountOwnershipType> getAllowedAccountOwnershipTypes() {
        return allowedAccountOwnershipTypes;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getOrganisationId() {
        return organisationId;
    }

    public static final class Builder {
        private final Set<PartyEndpoint> partyEndpoints = new HashSet<>();
        private URL apiBaseURL;
        private URL wellKnownURL;
        private Set<AccountOwnershipType> allowedAccountOwnershipTypes =
                Collections.singleton(AccountOwnershipType.PERSONAL);
        private String organisationId;

        private Builder() {}

        public Builder withApiBaseURL(final String apiBaseURL) {
            this.apiBaseURL = new URL(apiBaseURL);
            return this;
        }

        public Builder withWellKnownURL(final String wellKnownURL) {
            this.wellKnownURL = new URL(wellKnownURL);
            return this;
        }

        public Builder withPartyEndpoints(final PartyEndpoint... partyEndpoints) {
            if (partyEndpoints != null) {
                this.partyEndpoints.addAll(Arrays.asList(partyEndpoints));
            }
            return this;
        }

        public Builder withAllowedAccountOwnershipType(
                final AccountOwnershipType... allowedAccountOwnershipTypes) {
            this.allowedAccountOwnershipTypes = Sets.newHashSet(allowedAccountOwnershipTypes);
            return this;
        }

        public Builder withOrganisationId(final String organisationId) {
            this.organisationId = organisationId;
            return this;
        }

        public UkOpenBankingAisConfiguration build() {
            Preconditions.checkNotNull(apiBaseURL);
            Preconditions.checkNotNull(organisationId);
            Preconditions.checkNotNull(allowedAccountOwnershipTypes);
            Preconditions.checkNotNull(wellKnownURL);
            return new UkOpenBankingAisConfiguration(this);
        }
    }
}
