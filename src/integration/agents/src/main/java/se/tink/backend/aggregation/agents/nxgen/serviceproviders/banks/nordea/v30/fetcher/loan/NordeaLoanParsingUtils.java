package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan;

public class NordeaLoanParsingUtils {

    // The uniqueIdentifier is parsed this way because of how it was done in the Nordea legacy agent
    public static String loanIdToUniqueIdentifier(String loanId) {
        return loanId.substring(loanId.length() - 4);
    }
}
