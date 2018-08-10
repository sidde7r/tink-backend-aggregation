package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

public interface EuroInformationConfiguration {

    public String getUrl();

    public String getTarget();

    public String getAppVersion();

    public default String getLoginSubpage() {
        return EuroInformationConstants.Url.LOGIN;
    }

    public default String getLoginInit() {
        return EuroInformationConstants.Url.INIT;
    }

}
