package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaCreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaRemittanceInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;

final class BoursoramaPaymentRequestConverter {

    static BoursoramaCreatePaymentRequest convert(CreatePaymentRequest createPaymentRequest) {
        return BoursoramaCreatePaymentRequest.builder()
                .creationDateTime(createPaymentRequest.getCreationDateTime())
                .debtorAccount(createPaymentRequest.getDebtorAccount())
                .beneficiary(createPaymentRequest.getBeneficiary())
                .chargeBearer(createPaymentRequest.getChargeBearer())
                .creditTransferTransaction(map(createPaymentRequest.getCreditTransferTransaction()))
                .initiatingParty(createPaymentRequest.getInitiatingParty())
                .paymentInformationId(createPaymentRequest.getPaymentInformationId())
                .numberOfTransactions(createPaymentRequest.getNumberOfTransactions())
                .paymentTypeInformation(createPaymentRequest.getPaymentTypeInformation())
                .requestedExecutionDate(createPaymentRequest.getRequestedExecutionDate())
                .supplementaryData(createPaymentRequest.getSupplementaryData())
                .build();
    }

    private static List<BoursoramaCreditTransferTransactionEntity> map(
            List<CreditTransferTransactionEntity> creditTransferTransaction) {
        return creditTransferTransaction.stream()
                .map(BoursoramaPaymentRequestConverter::map)
                .collect(Collectors.toList());
    }

    private static BoursoramaCreditTransferTransactionEntity map(
            CreditTransferTransactionEntity entity) {
        return BoursoramaCreditTransferTransactionEntity.builder()
                .paymentId(entity.getPaymentId())
                .instructedAmount(BoursoramaAmountEntity.of(entity.getAmount()))
                .remittanceInformation(
                        BoursoramaRemittanceInformation.of(entity.getRemittanceInformation()))
                .build();
    }
}
