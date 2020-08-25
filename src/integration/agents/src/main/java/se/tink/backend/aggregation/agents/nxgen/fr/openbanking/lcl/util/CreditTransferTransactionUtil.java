package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.RemittanceInformation;

public class CreditTransferTransactionUtil {
    private CreditTransferTransactionUtil() {}

    private static CreditTransferTransactionEntity convert(
            se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base
                            .entities.CreditTransferTransactionEntity
                    creditTransferTransactionEntity) {
        RemittanceInformation remittanceInformation =
                new RemittanceInformation(
                        creditTransferTransactionEntity.getRemittanceInformation());
        return new CreditTransferTransactionEntity(
                creditTransferTransactionEntity.getPaymentId(),
                creditTransferTransactionEntity.getAmount(),
                remittanceInformation);
    }

    private static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking
                    .fropenbanking.base.entities.CreditTransferTransactionEntity
            toBase(CreditTransferTransactionEntity creditTransferTransactionEntity) {
        return new se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking
                .fropenbanking.base.entities.CreditTransferTransactionEntity(
                creditTransferTransactionEntity.getPaymentId(),
                creditTransferTransactionEntity.getAmount(),
                creditTransferTransactionEntity.getRemittanceInformation().getUnstructured());
    }

    public static List<
                    se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking
                            .fropenbanking.base.entities.CreditTransferTransactionEntity>
            toBaseList(List<CreditTransferTransactionEntity> creditTransferTransactionEntityList) {
        return creditTransferTransactionEntityList.stream()
                .map(CreditTransferTransactionUtil::toBase)
                .collect(Collectors.toList());
    }

    public static List<CreditTransferTransactionEntity> covertList(
            List<
                            se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking
                                    .fropenbanking.base.entities.CreditTransferTransactionEntity>
                    creditTransferTransactionEntities) {
        return creditTransferTransactionEntities.stream()
                .map(CreditTransferTransactionUtil::convert)
                .collect(Collectors.toList());
    }
}
