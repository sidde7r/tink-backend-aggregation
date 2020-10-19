package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.TransactionDetails;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaPartnerUtils {

    /**
     * Creates a serialized transaction details for inserting into the transaction payload. This is
     * a workaround due to not being able to send TransactionPayloadTypes.EXTERNAL_ID from
     * aggregation.
     *
     * @param transactionId
     * @return serialized transaction details
     */
    public static String getTransactionDetails(String transactionId) {
        HashMap<String, String> details = new HashMap<>();
        details.put(TransactionDetails.TRANSACTION_ID, transactionId);
        return SerializationUtils.serializeToString(details);
    }
}
