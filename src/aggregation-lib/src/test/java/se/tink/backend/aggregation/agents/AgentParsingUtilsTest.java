package se.tink.backend.aggregation.agents;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class AgentParsingUtilsTest {

    @Test
    public void testParseInterest() {
        Double d = AgentParsingUtils.parsePercentageFormInterest("1.62");
        Assert.assertEquals(d, 0.0162, 0.00001);

        d = AgentParsingUtils.parsePercentageFormInterest("1.62%");
        Assert.assertEquals(d, 0.0162, 0.00001);

        d = AgentParsingUtils.parsePercentageFormInterest("1,62%");
        Assert.assertEquals(d, 0.0162, 0.00001);

        d = AgentParsingUtils.parsePercentageFormInterest("1,62");
        Assert.assertEquals(d, 0.0162, 0.00001);

        d = AgentParsingUtils.parsePercentageFormInterest("0.62%");
        Assert.assertEquals(d, 0.0062, 0.00001);

        d = AgentParsingUtils.parsePercentageFormInterest("1,42 %");
        Assert.assertEquals(d, 0.0142, 0.00001);

        d = AgentParsingUtils.parsePercentageFormInterest("15%");
        Assert.assertEquals(d, 0.15, 0.00001);

        d = AgentParsingUtils.parsePercentageFormInterest(null);
        Assert.assertNull(d);
    }

    @Test
    @Parameters({
            "80Â 000.00, 80000",
            "21Â 094.60 , 21094.6",
            "23456.78, 23456.78"
    })
    public void parseAmount(String string, double expAmount) {
        assertThat(AgentParsingUtils.parseAmount(string)).isEqualTo(expAmount);
    }

    @Test
    public void parseNullAmoount() {
        assertThat(AgentParsingUtils.parseAmount(null)).isEqualTo(0);
    }
}
