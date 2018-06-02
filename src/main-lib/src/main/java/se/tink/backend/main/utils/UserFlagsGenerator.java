package se.tink.backend.main.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.core.ClientType;
import se.tink.backend.core.Market;

public class UserFlagsGenerator {
    private final FlagsConfiguration flagsConfiguration;

    @Inject
    public UserFlagsGenerator(FlagsConfiguration flagsConfiguration) {
        this.flagsConfiguration = flagsConfiguration;
    }

    public List<String> generateFlags(Market market, ClientType clientType, List<String> clientFlags) {
        Set<String> flags = Sets.newHashSet();

        if (clientFlags != null && !clientFlags.isEmpty()) {
            flags.addAll(clientFlags);
        }

        if (flagsConfiguration == null) {
            return Lists.newArrayList(flags);
        }

        final Map<String, Map<String, Double>> availableFlags = flagsConfiguration.getRegister();
        flags.addAll(FlagsGenerator.getDistributedFlags(availableFlags, market, Optional.ofNullable(clientType)));

        return Lists.newArrayList(flags);
    }
}
