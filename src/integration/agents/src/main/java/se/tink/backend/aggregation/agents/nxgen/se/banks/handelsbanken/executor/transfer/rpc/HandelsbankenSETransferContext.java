package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEAccountContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEPaymentAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSETransferContext extends TransferableResponse {

    private HandelsbankenSEAccountContext fromAccounts;
    private HandelsbankenSEAccountContext toAccounts;

    public Optional<HandelsbankenSEPaymentAccount> findSourceAccount(Transfer transfer) {
        return Optional.ofNullable(fromAccounts)
                .flatMap(fromAccounts -> fromAccounts.findSourceAccount(transfer));
    }

    public Optional<HandelsbankenSEPaymentAccount> findDestinationAccount(Transfer transfer) {
        return Optional.ofNullable(toAccounts)
                .flatMap(toAccounts -> toAccounts.findDestinationAccount(transfer));
    }

    public boolean destinationIsOwned(Transfer transfer) {
        return Optional.ofNullable(toAccounts)
                .map(toAccounts -> toAccounts.destinationIsOwned(transfer))
                .orElse(false);
    }

    public URL toValidateRecipient() {
        return findLink(HandelsbankenConstants.URLS.Links.VALIDATE_RECIPIENT);
    }

    public List<GeneralAccountEntity> retrieveOwnedSourceAccounts() {
        return Optional.ofNullable(fromAccounts)
                .flatMap(HandelsbankenSEAccountContext::asOwnedAccountEntities)
                .orElseGet(Collections::emptyList);
    }

    public List<GeneralAccountEntity> retrieveDestinationAccounts() {
        return Optional.ofNullable(toAccounts)
                .flatMap(HandelsbankenSEAccountContext::asGeneralAccountEntities)
                .orElseGet(Collections::emptyList);
    }
}
