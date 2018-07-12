package se.tink.backend.aggregation.agents.nxgen.fr.banks.bmcedirect;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;

// Banque Marocaine du Commerce Extérieur
public class BmceDirectConstants extends EuroInformationConstants {
    public static final String URL = "https://www.bmcedirect.ma/fr/wsmobile/";
    public static final String TARGET = "CM";
    // TODO: In the bank app this version is set in the body of the message, not in header like for different agents using same serviceProvider
    // TODO: When it's possible we need to doublecheck if setting it in header and not body does not affect behaviour
    public static final String APP_VERSION = "3.50.0.42";
}
