package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.LclCreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.RemittanceInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.CreditTransferTransactionEntity;

public class CreditTransferTransactionUtil {
    private CreditTransferTransactionUtil() {}

    private static LclCreditTransferTransactionEntity convert(
            CreditTransferTransactionEntity creditTransferTransactionEntity) {
        RemittanceInformation remittanceInformation =
                new RemittanceInformation(
                        creditTransferTransactionEntity.getRemittanceInformation());
        return new LclCreditTransferTransactionEntity(
                creditTransferTransactionEntity.getPaymentId(),
                creditTransferTransactionEntity.getAmount(),
                remittanceInformation);
    }

    private static CreditTransferTransactionEntity toBase(
            LclCreditTransferTransactionEntity creditTransferTransactionEntity) {
        return new CreditTransferTransactionEntity(
                creditTransferTransactionEntity.getPaymentId(),
                creditTransferTransactionEntity.getAmount(),
                creditTransferTransactionEntity.getRemittanceInformation().getUnstructured());
    }

    public static List<CreditTransferTransactionEntity> toBaseList(
            List<LclCreditTransferTransactionEntity> creditTransferTransactionEntityList) {
        return creditTransferTransactionEntityList.stream()
                .map(CreditTransferTransactionUtil::toBase)
                .collect(Collectors.toList());
    }

    public static List<LclCreditTransferTransactionEntity> convertList(
            List<CreditTransferTransactionEntity> creditTransferTransactionEntities) {
        return creditTransferTransactionEntities.stream()
                .map(CreditTransferTransactionUtil::convert)
                .collect(Collectors.toList());
    }
}
