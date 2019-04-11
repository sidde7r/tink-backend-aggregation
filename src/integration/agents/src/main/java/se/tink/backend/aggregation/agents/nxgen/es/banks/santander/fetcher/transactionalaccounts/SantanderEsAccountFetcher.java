package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderEsAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SantanderEsAccountFetcher.class);

    private final SessionStorage sessionStorage;

    public SantanderEsAccountFetcher(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String loginResponseString =
                sessionStorage
                        .get(SantanderEsConstants.Storage.LOGIN_RESPONSE, String.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SantanderEsConstants.LogMessages
                                                        .LOGIN_RESPONSE_NOT_FOUND));

        // Temporary logging of whole response in String format to detect any unknown accounts
        LOGGER.info("es_santander_full_logging_response", loginResponseString);
        LoginResponse loginResponse =
                SantanderEsXmlUtils.parseXmlStringToJson(loginResponseString, LoginResponse.class);

        return loginResponse.getAccountList().stream()
                .filter(AccountEntity::isKnownAccountType)
                .map(accountEntity -> accountEntity.toTinkAccount(loginResponse))
                .collect(Collectors.toList());
    }
}
