package se.tink.backend.aggregation.agents.nxgen.fr.banks.cic;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;

public class CicBankConstants extends EuroInformationConstants {
    public static final String URL = "https://mobile.cic.fr/wsmobile/fr/";
    public static final String TARGET = "CIC";
    // As this version seems to a bit old it would be good to check if this agent works as there is possibility that
    // some fields are missing
    public static final String APP_VERSION = "3.51.0.527";
}
