package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

public interface BnpParibasConfigurationBase {

    String getHost();

    String getUserAgent();

    String getGridType();

    String getDistId();

    String getAppVersion();

    String getBuildNumber();

    String getNumpadLastDigitIndex();

    String getTriAvValue();

    String getPastOrPendingValue();
}
