package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentInstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

public class SebInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final SebApiClient client;

    public SebInvestmentFetcher(SebApiClient client) {
        this.client = checkNotNull(client);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        Response accountListResponse = client.fetchInvestmentAccounts();
        List<InvestmentEntity> investments = accountListResponse.getInvestments();
        final List<InvestmentAccount> res = new ArrayList<>(investments.size());

        investments.forEach(
                (entity) -> {
                    Response response = client.fetchInvestmentDetails(entity.getDetailHandle());

                    List<InstrumentModule> instrumentModules =
                            response.getInvestmentInstruments().stream()
                                    .map(InvestmentInstrumentEntity::toTinkInstrument)
                                    .collect(Collectors.toList());
                    res.add(entity.toTinkAccount(instrumentModules));
                });

        return res;
    }
}
