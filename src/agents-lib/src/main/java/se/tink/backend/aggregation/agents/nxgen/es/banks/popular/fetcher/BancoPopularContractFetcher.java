package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.SetContractRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.SetContractResponse;

public class BancoPopularContractFetcher {

    protected final BancoPopularApiClient bankClient;
    protected final BancoPopularPersistentStorage persistentStorage;

    public BancoPopularContractFetcher(BancoPopularApiClient bankClient,
            BancoPopularPersistentStorage persistentStorage) {

        this.bankClient = bankClient;
        this.persistentStorage = persistentStorage;
    }

    protected Collection<BancoPopularContract> fetchContracts() {

        return persistentStorage.getLoginContracts().getContracts();
    }

    protected boolean selectCurrentContract(BancoPopularContract contract) {

        SetContractResponse setContractResponse = setCurrentContract(contract);

        return setContractResponse.isSuccess();
    }

    private SetContractResponse setCurrentContract(BancoPopularContract contract) {
        return bankClient.setContract(
                SetContractRequest.build(contract, persistentStorage.getIp()));
    }
}
