package se.tink.backend.aggregation.agents.utils.remittanceinformation;

import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RemittanceInformationUtils {

    private RemittanceInformationUtils() {}

    public static RemittanceInformation generateUnstructuredRemittanceInformation(String value) {
        RemittanceInformation result = new RemittanceInformation();
        result.setType(RemittanceInformationType.UNSTRUCTURED);
        result.setValue(value);
        return result;
    }
}
