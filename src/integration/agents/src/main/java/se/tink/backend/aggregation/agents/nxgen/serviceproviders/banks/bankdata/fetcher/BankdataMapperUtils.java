package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankdataMapperUtils {

    public static String getAccountNumberToDisplay(String regNo, String accountNo) {
        // this is how account number is presented in mobile app
        return String.format("%s %s", regNo, accountNo);
    }
}
