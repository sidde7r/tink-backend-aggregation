package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata;

import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountOwnerEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class IcaBankenIdentityDataFetcher implements IdentityDataFetcher {

    private final IcaBankenApiClient apiClient;
    private final String ssnFromCredential;

    public IcaBankenIdentityDataFetcher(IcaBankenApiClient apiClient, String ssnFromCredential) {

        this.apiClient = apiClient;
        this.ssnFromCredential = ssnFromCredential;
    }

    @Override
    public IdentityData fetchIdentityData() {
        List<AccountEntity> ownAccounts = apiClient.fetchAccounts().getOwnAccounts();

        // Icabanken splits accounts types on "own", "joint", and "minor". This should make it safe
        // to use the account owner data from one of the own account entities for identity data.
        if (!CollectionUtils.isEmpty(ownAccounts)) {
            AccountEntity ownAccountEntity = ownAccounts.get(0);
            AccountOwnerEntity accountOwner = ownAccountEntity.getAccountOwner();

            if (Objects.nonNull(accountOwner)) {
                return SeIdentityData.of(
                        accountOwner.getFullName(), accountOwner.getNationalIdWithCentury());
            }
        }

        // Use SSN from credential if user has no own accounts with usable data
        return SeIdentityData.of("", ssnFromCredential);
    }

    public FetchIdentityDataResponse getIdentityDataResponse() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
