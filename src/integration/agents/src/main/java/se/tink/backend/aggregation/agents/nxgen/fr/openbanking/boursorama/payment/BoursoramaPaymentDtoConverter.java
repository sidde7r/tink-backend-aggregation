package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaCreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaGetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaRemittanceInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;

final class BoursoramaPaymentDtoConverter {

    static BoursoramaCreatePaymentRequest convert(CreatePaymentRequest createPaymentRequest) {
        return BoursoramaCreatePaymentRequest.builder()
                .creationDateTime(createPaymentRequest.getCreationDateTime())
                .debtorAccount(createPaymentRequest.getDebtorAccount())
                .beneficiary(createPaymentRequest.getBeneficiary())
                .chargeBearer(createPaymentRequest.getChargeBearer())
                .creditTransferTransaction(
                        mapRequestDto(createPaymentRequest.getCreditTransferTransaction()))
                .initiatingParty(createPaymentRequest.getInitiatingParty())
                .paymentInformationId(createPaymentRequest.getPaymentInformationId())
                .numberOfTransactions(createPaymentRequest.getNumberOfTransactions())
                .paymentTypeInformation(createPaymentRequest.getPaymentTypeInformation())
                .requestedExecutionDate(createPaymentRequest.getRequestedExecutionDate())
                .supplementaryData(createPaymentRequest.getSupplementaryData())
                .build();
    }

    static GetPaymentResponse convert(BoursoramaGetPaymentResponse response) {
        final BoursoramaPaymentEntity request = response.getPaymentRequest();
        PaymentEntity entity =
                PaymentEntity.builder()
                        .beneficiary(request.getBeneficiary())
                        .creditTransferTransaction(
                                mapResponseDto(request.getCreditTransferTransaction()))
                        .debtorAccount(request.getDebtorAccount())
                        .paymentInformationStatus(request.getPaymentInformationStatus())
                        .paymentTypeInformation(request.getPaymentTypeInformation())
                        .statusReasonInformation(request.getStatusReasonInformation())
                        .build();
        return new GetPaymentResponse(entity);
    }

    private static List<BoursoramaCreditTransferTransactionEntity> mapRequestDto(
            List<CreditTransferTransactionEntity> creditTransferTransaction) {
        return creditTransferTransaction.stream()
                .map(BoursoramaPaymentDtoConverter::mapRequestDto)
                .collect(Collectors.toList());
    }

    private static BoursoramaCreditTransferTransactionEntity mapRequestDto(
            CreditTransferTransactionEntity entity) {
        return BoursoramaCreditTransferTransactionEntity.builder()
                .paymentId(entity.getPaymentId())
                .instructedAmount(BoursoramaAmountEntity.of(entity.getAmount()))
                .remittanceInformation(
                        BoursoramaRemittanceInformation.of(entity.getRemittanceInformation()))
                .build();
    }

    private static List<CreditTransferTransactionEntity> mapResponseDto(
            List<BoursoramaCreditTransferTransactionEntity> entities) {
        return entities.stream()
                .map(BoursoramaPaymentDtoConverter::mapResponseDto)
                .collect(Collectors.toList());
    }

    private static CreditTransferTransactionEntity mapResponseDto(
            BoursoramaCreditTransferTransactionEntity entity) {
        final BoursoramaAmountEntity responseAmount = entity.getInstructedAmount();
        return CreditTransferTransactionEntity.builder()
                .paymentId(entity.getPaymentId())
                .amount(
                        new AmountEntity(
                                responseAmount.getAmount().toPlainString(),
                                responseAmount.getCurrency()))
                .remittanceInformation(entity.getRemittanceInformation().getUnstructured())
                .build();
    }
}
