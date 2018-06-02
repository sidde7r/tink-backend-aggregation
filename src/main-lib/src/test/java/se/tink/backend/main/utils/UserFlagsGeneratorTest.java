package se.tink.backend.main.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Maps;
import org.junit.Test;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.core.ClientType;
import se.tink.backend.core.Market;
import static org.assertj.core.api.Assertions.assertThat;

public class UserFlagsGeneratorTest {

    @Test
    public void generateFlags() throws Exception {
        Market market = new Market();
        market.setCode("SE");

        ImmutableList<String> clientFlags = ImmutableList.of("biceps", "triceps");

        Map<String, Map<String, Double>> registerFlags = Maps.newHashMap();
        registerFlags.put("SE", ImmutableMap.of("biceps", 1.0, "pecs", 1.0));

        FlagsConfiguration config = new FlagsConfiguration();
        config.setRegister(registerFlags);

        UserFlagsGenerator generator = new UserFlagsGenerator(config);

        List<String> flags = generator.generateFlags(market, ClientType.IOS, clientFlags);

        assertThat(flags).hasSize(3);
        assertThat(flags).containsOnly("biceps", "triceps", "pecs");
    }
}
