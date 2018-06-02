package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities.LoanAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse {
    private String number;
    private double initialBalanace;
    private double termAmount;
    private String nextTermDate;
    private double nominalInterestRate;
    private double effectiveInterestRate;
    private int termLength;
    private String discountDate;
    private String agreedClosureDate;
    private String initialClosureDate;
    private String invoiceAccount;
    private LoanAccessEntity access;

    public String getNumber() {
        return number;
    }

    public double getInitialBalanace() {
        return initialBalanace;
    }

    public double getNominalInterestRate() {
        return nominalInterestRate;
    }

    public LoanAccessEntity getAccess() {
        return access;
    }
}
