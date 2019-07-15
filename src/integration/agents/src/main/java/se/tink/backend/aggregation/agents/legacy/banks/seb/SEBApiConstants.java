package se.tink.backend.aggregation.agents.banks.seb;

public class SEBApiConstants {

    public static class PortfolioType {
        public static final String OCCUPATIONAL_PENSION = "tjänstepension";
        public static final String ENDOWMENT_INSURANCE = "kapitalförsäkring";
        public static final String PENSION_SAVINGS_FUND = "pensionsspar fond";
        public static final String SAFE_PENSION_INSURANCE = "trygg privatpension";
        public static final String PENSION_INSURANCE = "pensionsförsäkring";
    }

    public static class SystemCode {
        public static final int BANKID_NOT_AUTHORIZED = 2;
        public static final int KYC_ERROR = 9200;
    }
}
