package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.HolderNames;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.ParticipantAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.SavingAccountEntity;
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
        Optional<PositionEntity> positionEntity = apiClient.getPositions();
        return Stream.concat(
                        mapCheckingAccounts(positionEntity).stream(),
                        mapSavingsAccounts(positionEntity).stream())
                .collect(Collectors.toList());
    }

    private Collection<TransactionalAccount> mapCheckingAccounts(
            Optional<PositionEntity> positionEntity) {
        return positionEntity
                .map(PositionEntity::getAccounts)
                .map(mapToTinkCheckingAccounts())
                .get();
    }

    private Function<List<AccountEntity>, List<TransactionalAccount>> mapToTinkCheckingAccounts() {
        return accountEntities ->
                accountEntities.stream()
                        .map(
                                accountEntity ->
                                        accountEntity.toTinkTransactionalAccount(
                                                fetchParticipants(accountEntity)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
    }

    private Collection<TransactionalAccount> mapSavingsAccounts(
            Optional<PositionEntity> positionEntity) {
        if (positionEntity.isPresent()) {
            return positionEntity
                    .map(PositionEntity::getSavingInvestment)
                    .map(mapToTinkSavingsAccounts(positionEntity.get().getCurrency()))
                    .get();
        }
        return Collections.emptyList();
    }

    private Function<List<SavingAccountEntity>, List<TransactionalAccount>>
            mapToTinkSavingsAccounts(String currency) {
        return savingEntities ->
                savingEntities.stream()
                        .map(
                                savingAccountEntity ->
                                        savingAccountEntity.toTinkTransactionalAccount(currency))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
    }

    private List<Party> fetchParticipants(AccountEntity account) {
        return apiClient
                .fetchAccountInfo(account.getAccountId())
                .getAccountParticipants()
                .map(this::getHolderName)
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
