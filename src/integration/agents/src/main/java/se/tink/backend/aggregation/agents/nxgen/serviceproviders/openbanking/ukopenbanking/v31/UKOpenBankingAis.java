package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Getter
public class UKOpenBankingAis implements UkOpenBankingAisConfig {
    private final URL apiBaseURL;
    private final URL wellKnownURL;
    private final URL appToAppURL;
    private final URL identityDataURL;
    private final boolean partyEndpointEnabled;
    private final boolean accountPartyEndpointEnabled;
    private final boolean accountPartiesEndpointEnabled;

    private final List<String> additionalPermissions;
    private IdentityDataEntity identityData;
    private String holderName;
    private AccountOwnershipType allowedAccountOwnershipType;
    private final String organisationId;

    private UKOpenBankingAis(
            URL apiBaseURL,
            URL wellKnownURL,
            URL identityDataURL,
            URL appToAppURL,
            boolean partyEndpointEnabled,
            boolean accountPartyEndpointEnabled,
            boolean accountPartiesEndpointEnabled,
            List<String> additionalPermissions,
            AccountOwnershipType allowedAccountOwnershipType,
            String organisationId) {
        this.apiBaseURL = apiBaseURL;
        this.wellKnownURL = wellKnownURL;
        this.identityDataURL = identityDataURL;
        this.appToAppURL = appToAppURL;
        this.partyEndpointEnabled = partyEndpointEnabled;
        this.accountPartyEndpointEnabled = accountPartyEndpointEnabled;
        this.accountPartiesEndpointEnabled = accountPartiesEndpointEnabled;
        this.additionalPermissions = additionalPermissions;
        this.allowedAccountOwnershipType = allowedAccountOwnershipType;
        this.organisationId = organisationId;
    }

    @Override
    public void setIdentityData(IdentityDataEntity identityData) {
        this.identityData = identityData;
    }

    @Override
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    @Override
    public String getIntentId(AccountPermissionResponse accountPermissionResponse) {
        AccountPermissionResponseV31 accountPermissionResponseV31 =
                (AccountPermissionResponseV31) accountPermissionResponse;
        return accountPermissionResponseV31.getData().getAccountConsentId();
    }

    @Override
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
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    @Override
    public AccountOwnershipType getAllowedAccountOwnershipType() {
        return allowedAccountOwnershipType;
    }

    @Override
    public String getOrganisationId() {
        return null;
    }

    // TODO replace with lombok builder
    public static final class Builder {

        private URL apiBaseURL;
        private URL wellKnownURL;
        private URL identityDataURL;
        private URL appToAppURL;
        private List<String> additionalPermissions;
        private boolean partyEndpointEnabled;
        private boolean accountPartyEndpointEnabled;
        private boolean accountPartiesEndpointEnabled;
        private AccountOwnershipType allowedAccountOwnershipType = AccountOwnershipType.PERSONAL;
        private String organisationId;

        public Builder() {}

        public static Builder builder() {
            return new Builder();
        }

        public Builder withApiBaseURL(final String apiBaseURL) {
            this.apiBaseURL = new URL(apiBaseURL);
            return this;
        }

        public Builder withWellKnownURL(final String wellKnownURL) {
            this.wellKnownURL = new URL(wellKnownURL);
            return this;
        }

        public Builder withIdentityDataURL(final String identityDataURL) {
            this.identityDataURL = new URL(identityDataURL);
            // TODO replace this builder method with URL with 3 seperate boolean methods to enable
            // each endpoints
            if (identityDataURL.equals(PartyEndpoints.IDENTITY_DATA_ENDPOINT_PARTY)) {
                partyEndpointEnabled = true;
            } else if (identityDataURL.equals(
                    PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY)) {
                accountPartyEndpointEnabled = true;
            } else if (identityDataURL.equals(
                    PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES)) {
                accountPartiesEndpointEnabled = true;
            }
            return this;
        }

        public Builder withAppToAppURL(final String appToAppURL) {
            this.appToAppURL = new URL(appToAppURL);
            return this;
        }

        public Builder withAdditionalPermission(final String additionalPermission) {
            if (Objects.isNull(this.additionalPermissions)) {
                this.additionalPermissions = new ArrayList<>();
            }
            this.additionalPermissions.add(additionalPermission);
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

            return new UKOpenBankingAis(
                    apiBaseURL,
                    wellKnownURL,
                    identityDataURL,
                    appToAppURL,
                    partyEndpointEnabled,
                    accountPartyEndpointEnabled,
                    accountPartiesEndpointEnabled,
                    additionalPermissions,
                    allowedAccountOwnershipType,
                    organisationId);
        }
    }
}
