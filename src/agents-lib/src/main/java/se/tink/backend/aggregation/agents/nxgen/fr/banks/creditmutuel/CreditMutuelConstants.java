package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;

public class CreditMutuelConstants extends EuroInformationConstants {
    public static final String URL = "https://mobile.creditmutuel.fr/wsmobile/fr/";
    public static final String TARGET = "CM";
    public static final String APP_VERSION = "3.51.0.527";

    public static class RequestBodyValues {
        //Credit Cards
        public static final String SPID = "spid_version";
        public static final String SPID_VALUE = "3.0.0";
    }

    public static class Url {
        public static final String CREDIT_CARD_ACCOUNTS = "SCIM_default.aspx";
        public static final String CREDIT_CARD_TRANSACTIONS = "banque/CRP8_SCIM_DEPCAR.aspx";

    }
}
