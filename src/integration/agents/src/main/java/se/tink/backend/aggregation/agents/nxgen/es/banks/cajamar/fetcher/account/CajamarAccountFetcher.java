package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.HolderNames;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.ParticipantAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
public class CajamarAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CajamarApiClient apiClient;

    public CajamarAccountFetcher(CajamarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        PositionEntity positionEntity = apiClient.fetchPositions();
        return Stream.concat(
                        mapCheckingAccounts(positionEntity), mapSavingsAccounts(positionEntity))
                .collect(Collectors.toList());
    }

    private Stream<TransactionalAccount> mapCheckingAccounts(PositionEntity positionEntity) {
        return positionEntity.getAccounts().stream()
                .map(
                        accountEntity ->
                                accountEntity.toTinkTransactionalAccount(
                                        fetchParticipants(accountEntity)))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<TransactionalAccount> mapSavingsAccounts(PositionEntity positionEntity) {
        String currency = positionEntity.getCurrency();
        return positionEntity.getSavingInvestment().stream()
                .map(savingInvestment -> savingInvestment.toTinkTransactionalAccount(currency))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private List<Party> fetchParticipants(AccountEntity account) {
        return apiClient
                .fetchAccountInfo(account.getAccountId())
                .getAccountParticipants()
                .map(participant -> getHolderName(participant))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Party> getHolderName(ParticipantAccountEntity participant) {
        switch (participant.getAssociation()) {
            case HolderNames.OWNER:
                return Optional.of(new Party(participant.getName(), Role.HOLDER));
            case HolderNames.AUTHORIZED:
                return Optional.of(new Party(participant.getName(), Role.AUTHORIZED_USER));
            default:
                log.info(
                        "Participant {} with association {}",
                        participant.getName(),
                        participant.getAssociation());
                return Optional.of(new Party(participant.getName(), Role.OTHER));
        }
    }
}
