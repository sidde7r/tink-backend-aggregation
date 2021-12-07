package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.identitydata;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@Slf4j
@RequiredArgsConstructor
public class NorwegianIdentityDataFetcher implements IdentityDataFetcher {
    private final NorwegianApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        final AccountsResponse accountsResponse = apiClient.fetchAccounts();

        return IdentityData.builder()
                .setFullName(getOwnerName(accountsResponse))
                .setDateOfBirth(null)
                .build();
    }

    private String getOwnerName(AccountsResponse accountsResponse) {

        Set<String> holderNames =
                accountsResponse.getAccounts().stream()
                        .map(AccountsItemEntity::getOwnerName)
                        .collect(Collectors.toSet());

        if (holderNames.size() > 1) {
            log.info("Multiple holder names found");
        }

        return holderNames.stream().findFirst().orElse(StringUtils.EMPTY);
    }
}
