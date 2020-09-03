package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.account;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.converter.BanquePopulaireConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BanquePopulaireAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final BanquePopulaireApiClient banquePopulaireApiClient;
    private final BanquePopulaireConverter banquePopulaireConverter;

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return banquePopulaireApiClient.fetchAccounts().stream()
                .map(banquePopulaireConverter::convertAccountDtoToTinkTransactionalAccount)
                .collect(Collectors.toList());
    }
}
