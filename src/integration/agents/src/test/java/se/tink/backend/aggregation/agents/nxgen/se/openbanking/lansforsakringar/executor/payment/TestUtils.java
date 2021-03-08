package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes.CROSS_BORDER_CREDIT_TRANSFERS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes.DOMESTIC_CREDIT_TRANSFERS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes.DOMESTIC_GIROS_RESPONSE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountNumbersResponse;

public class TestUtils {

    public static AccountNumbersResponse getAccountNumbersResponse(String bban1, String bban2) {
        List<String> allowedTransactionTypes1 =
                Arrays.asList(DOMESTIC_GIROS_RESPONSE, DOMESTIC_CREDIT_TRANSFERS_RESPONSE);
        AccountInfoEntity accountInfoEntity1 =
                new AccountInfoEntity(bban1, allowedTransactionTypes1);

        List<String> allowedTransactionTypes2 =
                Collections.singletonList(CROSS_BORDER_CREDIT_TRANSFERS_RESPONSE);
        AccountInfoEntity accountInfoEntity2 =
                new AccountInfoEntity(bban2, allowedTransactionTypes2);

        return new AccountNumbersResponse(Arrays.asList(accountInfoEntity1, accountInfoEntity2));
    }

    public static AccountNumbersResponse getDomesticAccountNumbersResponse(
            String bban0, String bban1, String bban2) {

        List<String> allowedTransactionTypes0 =
                Collections.singletonList(CROSS_BORDER_CREDIT_TRANSFERS_RESPONSE);
        AccountInfoEntity accountInfoEntity0 =
                new AccountInfoEntity(bban0, allowedTransactionTypes0);

        List<String> allowedTransactionTypes1 =
                Arrays.asList(
                        CROSS_BORDER_CREDIT_TRANSFERS_RESPONSE, DOMESTIC_CREDIT_TRANSFERS_RESPONSE);
        AccountInfoEntity accountInfoEntity1 =
                new AccountInfoEntity(bban1, allowedTransactionTypes1);

        List<String> allowedTransactionTypes2 =
                Arrays.asList(DOMESTIC_CREDIT_TRANSFERS_RESPONSE, DOMESTIC_GIROS_RESPONSE);
        AccountInfoEntity accountInfoEntity2 =
                new AccountInfoEntity(bban2, allowedTransactionTypes2);

        return new AccountNumbersResponse(
                Arrays.asList(accountInfoEntity0, accountInfoEntity1, accountInfoEntity2));
    }
}
