package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentBaseinfoResponse {
    private PaymentDestinationsEntity payment;
    private TransferDestinationsEntity transfer;
    private AllowedLinksEntity addRecipientStatus;
    private AllowedLinksEntity addPayeeStatus;
    private List<TransactionAccountGroupEntity> transactionAccountGroups;
    private InternationalRecipientsEntity internationalRecipients;

    public PaymentDestinationsEntity getPayment() {
        return payment;
    }

    public TransferDestinationsEntity getTransfer() {
        return transfer;
    }

    public AllowedLinksEntity getAddRecipientStatus() {
        return addRecipientStatus;
    }

    public AllowedLinksEntity getAddPayeeStatus() {
        return addPayeeStatus;
    }

    public List<TransactionAccountGroupEntity> getTransactionAccountGroups() {
        return transactionAccountGroups;
    }

    public InternationalRecipientsEntity getInternationalRecipients() {
        return internationalRecipients;
    }

    public List<? extends GeneralAccountEntity> getPaymentDestinations() {
        return Optional.ofNullable(payment)
                .map(PaymentDestinationsEntity::getPayees)
                .orElseGet(Collections::emptyList);
    }

    public List<? extends GeneralAccountEntity> getTransferDestinations() {
        return Optional.ofNullable(transfer)
                .map(TransferDestinationsEntity::getExternalRecipients)
                .orElseGet(Collections::emptyList);
    }

    public List<? extends GeneralAccountEntity> getPaymentSourceAccounts() {
        return getSourceAccounts(SwedbankBaseConstants.TransferScope.PAYMENT_FROM);
    }

    public List<? extends GeneralAccountEntity> getTransferSourceAccounts() {
        return getSourceAccounts(SwedbankBaseConstants.TransferScope.TRANSFER_FROM);
    }

    private List<? extends GeneralAccountEntity> getSourceAccounts(String scope) {
        return Optional.ofNullable(transactionAccountGroups)
                .orElseGet(Collections::emptyList).stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(tdae -> tdae.scopesContainsIgnoreCase(scope))
                .collect(Collectors.toList());
    }
}
