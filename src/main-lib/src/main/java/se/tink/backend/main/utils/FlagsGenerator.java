package se.tink.backend.main.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import se.tink.backend.core.ClientType;
import se.tink.backend.core.Market;

public class FlagsGenerator {

    public static List<String> getDistributedFlags(Map<String, Map<String, Double>> flagsByMarket, Market market,
            Optional<ClientType> clientType) {

        if (flagsByMarket == null || flagsByMarket.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> availableFlags = Maps.newHashMap();

        // Add market specific flags.

        Map<String, Double> marketFlags = null;

        if (market != null) {
            marketFlags = flagsByMarket.get(market.getCodeAsString());
        }

        if (marketFlags != null) {
            availableFlags.putAll(marketFlags);
        }

        // Add general flags.

        Map<String, Double> generalFlags = flagsByMarket.get("ALL");

        if (generalFlags != null) {
            availableFlags.putAll(generalFlags);
        }

        return getDistributedFlags(availableFlags, clientType);
    }

    public static List<String> getDistributedFlags(Map<String, Double> availableFlags) {
        return getDistributedFlags(availableFlags, Optional.empty());
    }

    public static List<String> getDistributedFlags(Map<String, Double> availableFlags, Optional<ClientType> clientType) {

        Random random = new Random();
        List<String> flags = Lists.newArrayList();

        // Evaluate which flags to dynamically add.

        for (Map.Entry<String, Double> registerFlag : availableFlags.entrySet()) {
            String flag = registerFlag.getKey();
            boolean flagEnabled = (random.nextDouble() <= registerFlag.getValue());

            // Check platform specific flag.

            if (flag.contains("ANDROID")) {
                if (!clientType.isPresent() || !Objects.equals(clientType.get(), ClientType.ANDROID)) {
                    continue;
                }
            }
            if (flag.contains("IOS")) {
                if (!clientType.isPresent() || !Objects.equals(clientType.get(), ClientType.IOS)) {
                    continue;
                }
            }

            // Check test flag on or off.

            if (flag.startsWith("TEST_")) {
                if (flagEnabled) {
                    flags.add(flag + "_ON");
                } else {
                    flags.add(flag + "_OFF");
                }
            } else if (flagEnabled) {
                flags.add(flag);
            }
        }

        return flags;
    }
}
