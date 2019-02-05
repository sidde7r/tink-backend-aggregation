package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class HandelsbankenSEAccountContext {

    private List<HandelsbankenSEAccountGroup> accountGroups;

    public Optional<HandelsbankenSEPaymentAccount> findSourceAccount(Transfer transfer) {
        return findOwnAccount().flatMap(accountGroup -> accountGroup.findSourceAccount(transfer));
    }

    public Optional<HandelsbankenSEPaymentAccount> findDestinationAccount(Transfer transfer) {
        Optional<HandelsbankenSEPaymentAccount> ownAccount = findOwnDestination(transfer);
        if (ownAccount.isPresent()) {
            return ownAccount;
        }
        return findAccountHaving(HandelsbankenSEAccountGroup::isOtherRecipientAccount).flatMap(toDestination(transfer));
    }

    private Optional<HandelsbankenSEPaymentAccount> findOwnDestination(Transfer transfer) {
        return findOwnAccount().flatMap(toDestination(transfer));
    }

    private Function<HandelsbankenSEAccountGroup, Optional<HandelsbankenSEPaymentAccount>> toDestination(
            Transfer transfer) {
        return accountGroup -> accountGroup.findDestinationAccount(transfer);
    }

    private Optional<HandelsbankenSEAccountGroup> findOwnAccount() {
        return findAccountHaving(HandelsbankenSEAccountGroup::isOwnAccount);
    }

    private Optional<HandelsbankenSEAccountGroup> findAccountHaving(Predicate<HandelsbankenSEAccountGroup> predicate) {
        return Optional.ofNullable(accountGroups)
                .map(Collection::stream)
                .flatMap(groups -> groups
                        .filter(predicate)
                        .findFirst());
    }

    public boolean destinationIsOwned(Transfer transfer) {
        return findOwnDestination(transfer).isPresent();
    }

    public Optional<List<GeneralAccountEntity>> asOwnedAccountEntities() {
        return Optional.ofNullable(accountGroups)
                .map(Collection::stream)
                .map(accountGroups -> accountGroups
                        .filter(HandelsbankenSEAccountGroup::isOwnAccount)
                        .flatMap(HandelsbankenSEAccountGroup::asGeneralAccountEntities)
                        .collect(Collectors.toList())
                );
    }

    public Optional<List<GeneralAccountEntity>> asGeneralAccountEntities() {
        return Optional.ofNullable(accountGroups)
                .map(Collection::stream)
                .map(accountGroups -> accountGroups
                        .flatMap(HandelsbankenSEAccountGroup::asGeneralAccountEntities)
                        .collect(Collectors.toList())
                );
    }
}
