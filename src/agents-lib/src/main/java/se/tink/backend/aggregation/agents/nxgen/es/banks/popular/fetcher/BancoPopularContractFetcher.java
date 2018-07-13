package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularPersistenStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.SetContractRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.SetContractResponse;

public class BancoPopularContractFetcher {

    protected final BancoPopularApiClient bankClient;
    protected final BancoPopularPersistenStorage persistentStorage;

    public BancoPopularContractFetcher(BancoPopularApiClient bankClient,
            BancoPopularPersistenStorage persistentStorage) {

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
        SetContractRequest setContractRequest = new SetContractRequest()
                .setBanco(contract.getBanco())
                .setOficina(contract.getOficina())
                .setContract(contract.getnItnCont())
                .setIp(persistentStorage.getIp());

        return bankClient.setContract(setContractRequest);
    }
}
