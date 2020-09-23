package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UKOpenBankingAis implements UkOpenBankingAisConfig {
    private final URL apiBaseURL;
    private final URL wellKnownURL;
    private final URL appToAppURL;
    private final Set<PartyEndpoint> partyEndpoints;
    private final AccountOwnershipType allowedAccountOwnershipType;
    private final String organisationId;

    private UKOpenBankingAis(Builder builder) {
        this.apiBaseURL = builder.apiBaseURL;
        this.wellKnownURL = builder.wellKnownURL;
        this.appToAppURL = builder.appToAppURL;
        this.partyEndpoints = builder.partyEndpoints;
        this.allowedAccountOwnershipType = builder.allowedAccountOwnershipType;
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
        return apiBaseURL.concat(ACCOUNT_BULK_REQUEST);
    }

    @Override
    public URL getAccountBalanceRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public URL getAccountBeneficiariesRequestURL(String accountId) {
        return apiBaseURL.concat("/accounts").concat("/" + accountId).concat("/beneficiaries");
    }

    @Override
    public URL createConsentRequestURL() {
        return apiBaseURL.concat(UkOpenBankingV31Constants.ApiServices.CONSENT_REQUEST);
    }

    @Override
    public URL getUpcomingTransactionRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
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
        return partyEndpoints.contains(PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY);
    }

    @Override
    public boolean isAccountPartiesEndpointEnabled() {
        return partyEndpoints.contains(PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES);
    }

    @Override
    public boolean isAccountPartyEndpointEnabled() {
        return partyEndpoints.contains(PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY);
    }

    @Override
    public URL getAppToAppURL() {
        return this.appToAppURL;
    }

    @Override
    public Set<String> getAdditionalPermissions() {
        return this.partyEndpoints.stream()
                .flatMap(partyEndpoint -> partyEndpoint.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    @Override
    public AccountOwnershipType getAllowedAccountOwnershipType() {
        return allowedAccountOwnershipType;
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
        private URL appToAppURL;
        private AccountOwnershipType allowedAccountOwnershipType = AccountOwnershipType.PERSONAL;
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

        public Builder withAppToAppURL(final String appToAppURL) {
            this.appToAppURL = new URL(appToAppURL);
            return this;
        }

        public Builder withPartyEndpoints(final PartyEndpoint... partyEndpoints) {
            if (partyEndpoints != null) {
                this.partyEndpoints.addAll(Arrays.asList(partyEndpoints));
            }
            return this;
        }

        public Builder withAllowedAccountOwnershipType(
                final AccountOwnershipType allowedAccountOwnershipType) {
            this.allowedAccountOwnershipType = allowedAccountOwnershipType;
            return this;
        }

        public Builder withOrganisationId(final String organisationId) {
            this.organisationId = organisationId;
            return this;
        }

        public UKOpenBankingAis build() {
            Preconditions.checkNotNull(apiBaseURL);
            Preconditions.checkNotNull(organisationId);
            return new UKOpenBankingAis(this);
        }
    }
}
