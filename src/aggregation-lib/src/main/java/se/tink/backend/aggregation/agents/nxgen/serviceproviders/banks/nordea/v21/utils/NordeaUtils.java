package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.utils;

public class NordeaUtils {
    public static String maskAccountNumber(String accountNumber) {
        return "************" + accountNumber.substring(accountNumber.length() - 4);
    }
}
