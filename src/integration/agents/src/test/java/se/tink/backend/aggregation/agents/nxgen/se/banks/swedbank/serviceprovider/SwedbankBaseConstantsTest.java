package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.Authorization;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.InvestmentAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.LinkMethod;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SwedbankBaseConstantsTest {

    @Test
    public void shouldReturnUrlWithoutParameters() {
        URL result = SwedbankBaseConstants.Url.createDynamicUrl("host", "path", null);

        assertEquals("hostpath", result.getUrl().toString());
    }

    @Test
    public void shouldReturnEncodedString() {
        ProfileParameters profileParameters =
                new ProfileParameters("name", "apiKey", false, "userAgent");
        SwedbankConfiguration swedbankConfiguration =
                new SwedbankConfiguration(profileParameters, "host", false);
        String result =
                SwedbankBaseConstants.generateAuthorization(swedbankConfiguration, "username");

        assertEquals("YXBpS2V5OjRDMzU0RkMyLTJBQzItREI0Ni01QUQxLUYzQjBGM0Q0RDRFRQ", result);
    }

    @Test
    public void shouldReturnPortfolioTypeIfGivenTypeExist() {
        InvestmentAccountType result =
                SwedbankBaseConstants.InvestmentAccountType.fromAccountType("ISK");

        assertEquals(result, InvestmentAccountType.ISK);
    }

    @Test
    public void shouldReturnEmptyStringIfGivenTypeNull() {
        InvestmentAccountType result =
                SwedbankBaseConstants.InvestmentAccountType.fromAccountType(null);

        assertEquals(result, InvestmentAccountType.UNKNOWN);
        assertEquals("", InvestmentAccountType.UNKNOWN.getAccountType());
    }

    @Test
    public void shouldReturnEmptyStringIfGivenTypeNotKnown() {
        InvestmentAccountType result =
                SwedbankBaseConstants.InvestmentAccountType.fromAccountType("random string");

        assertEquals(result, InvestmentAccountType.UNKNOWN);
    }

    @Test
    public void shouldReturnStatusCodeIfGivenTypeExist() {
        Authorization result =
                SwedbankBaseConstants.Authorization.fromAuthorizationString("AUTHORIZED");

        assertEquals(result, Authorization.AUTHORIZED);
    }

    @Test
    public void shouldReturnStatusCodeIfGivenTypeUsesCapsStrangely() {
        Authorization result =
                SwedbankBaseConstants.Authorization.fromAuthorizationString("AutHoriZEd");

        assertEquals(result, Authorization.AUTHORIZED);
    }

    @Test
    public void shouldReturnEmptyStringIfGivenAuthorizationNotKnown() {
        Authorization result =
                SwedbankBaseConstants.Authorization.fromAuthorizationString("random string");

        assertEquals(result, Authorization.UNKNOWN);
        assertEquals("", Authorization.UNKNOWN.getAuthRequirement());
    }

    @Test
    public void shouldReturnEmptyStringIfGivenAuthorizationNull() {
        Authorization result = SwedbankBaseConstants.Authorization.fromAuthorizationString(null);

        assertEquals(result, Authorization.UNKNOWN);
        assertEquals("", Authorization.UNKNOWN.getAuthRequirement());
    }

    @Test
    public void shouldReturnEmptyStringIfGivenVerbNull() {
        LinkMethod result = SwedbankBaseConstants.LinkMethod.fromVerb(null);

        assertEquals(result, LinkMethod.UNKNOWN);
        assertEquals("", LinkMethod.UNKNOWN.getVerb());
    }

    @Test
    public void shouldReturnMethodIfGivenVerbExist() {
        LinkMethod result = SwedbankBaseConstants.LinkMethod.fromVerb("get");

        assertEquals(result, LinkMethod.GET);
    }

    @Test
    public void shouldReturnEmptyStringIfGivenVerbDontExist() {
        LinkMethod result = SwedbankBaseConstants.LinkMethod.fromVerb("random string");

        assertEquals(result, LinkMethod.UNKNOWN);
        assertEquals("", LinkMethod.UNKNOWN.getVerb());
    }
}
