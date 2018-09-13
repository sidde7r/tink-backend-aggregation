package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.entities.BancoPopularLoginContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContracts;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class BancoPopularAuthenticator implements PasswordAuthenticator {

    private final BancoPopularApiClient bankClient;
    private final BancoPopularPersistentStorage persistentStorage;

    public BancoPopularAuthenticator(BancoPopularApiClient bankClient, BancoPopularPersistentStorage persistentStorage) {
        this.bankClient = bankClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        LoginRequest loginRequest = LoginRequest.build(username, password);

        LoginResponse response = bankClient.login(loginRequest);

        List<BancoPopularLoginContract> loginContracts = response.getLoginContractOut();
        BancoPopularContracts contracts = new BancoPopularContracts();

        for (BancoPopularLoginContract loginContract : loginContracts) {
            contracts.addLoginContract(BancoPopularContract.build(loginContract));
       }

       // store for subsequent calls
       persistentStorage.setContracts(contracts);
    }
}
