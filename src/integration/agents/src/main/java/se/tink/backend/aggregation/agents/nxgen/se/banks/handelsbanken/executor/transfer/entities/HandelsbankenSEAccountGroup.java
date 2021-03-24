package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class HandelsbankenSEAccountGroup {

    private List<HandelsbankenSEPaymentAccount> accounts;
    private String type;

    public boolean isOwnAccount() {
        return "OWN_ACCOUNT".equalsIgnoreCase(type);
    }

    public boolean isOtherRecipientAccount() {
        return "OTHER_RECIPIENT_ACCOUNT".equalsIgnoreCase(type);
    }

    public Optional<HandelsbankenSEPaymentAccount> findSourceAccount(Transfer transfer) {
        return findAccount(transfer.getSource());
    }

    public Optional<HandelsbankenSEPaymentAccount> findDestinationAccount(Transfer transfer) {
        return findAccount(transfer.getDestination());
    }

    private Optional<HandelsbankenSEPaymentAccount> findAccount(AccountIdentifier identifier) {
        Optional<HandelsbankenSEPaymentAccount> paymentAccount =
                findPaymentAccount(
                        identifier.getIdentifier(new DefaultAccountIdentifierFormatter()));
        if (paymentAccount.isPresent()) {
            return paymentAccount;
        }
        if (identifier.getType() == AccountIdentifierType.SE) {
            SwedishIdentifier swedish = identifier.to(SwedishIdentifier.class);
            return findPaymentAccount(swedish.getAccountNumber());
        }
        return Optional.empty();
    }

    private Optional<HandelsbankenSEPaymentAccount> findPaymentAccount(String identifier) {
        return Optional.ofNullable(accounts)
                .map(Collection::stream)
                .flatMap(
                        accounts ->
                                accounts.filter(account -> account.hasIdentifier(identifier))
                                        .findFirst());
    }

    public Stream<GeneralAccountEntity> asGeneralAccountEntities() {
        return Optional.ofNullable(accounts)
                .map(Collection::stream)
                .map(
                        accounts ->
                                accounts.map(HandelsbankenSEPaymentAccount::toGeneralAccountEntity))
                .orElseGet(Stream::empty);
    }
}
