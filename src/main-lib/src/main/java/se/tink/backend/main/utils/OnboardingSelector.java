package se.tink.backend.main.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.core.DeviceOrigin;
import se.tink.backend.core.enums.FeatureFlags;

public class OnboardingSelector {
    private final FlagsConfiguration flagsConfiguration;
    private final static Random RANDOM = new Random();

    @Inject
    public OnboardingSelector(FlagsConfiguration flagsConfiguration) {
        this.flagsConfiguration = flagsConfiguration;
    }

    public List<String> getOnboadingFeatures(Optional<DeviceOrigin> origin, String desiredMarket) {

        Set<String> flags = Sets.newHashSet();

        final Map<String, Double> availableFlags = flagsConfiguration.getDevice();
        flags.addAll(FlagsGenerator.getDistributedFlags(availableFlags));

        Optional<String> campaign = getCampaignFeatureFlags(origin);

        if (!Strings.isNullOrEmpty(desiredMarket) && !desiredMarket.equalsIgnoreCase("se")) {
            // This is a fast and hacky fix to ensure `ONBOARDING_V1` for non-SE markets (i.e beta)
            // Todo: Remove this and have this in the configuration.
            flags.clear();
            flags.add(FeatureFlags.ONBOARDING_V1);
        } else if (campaign.isPresent()) {
            // Force v2 if a campaign has been identified; only v2 supports campaign specific onboarding.
            flags.add(FeatureFlags.ONBOARDING_V2);

            // One-off A/B test of the mortgage onboarding. Keep only the `else` section when test is done.
            if (Objects.equals("MORTGAGE", campaign.get())) {
                if (RANDOM.nextBoolean()) {
                    flags.add("ONBOARDING_" + campaign.get());
                    flags.add("TEST_ONBOARDING_MORTGAGE_ON");
                } else {
                    flags.add("TEST_ONBOARDING_MORTGAGE_OFF");
                }
            } else {
                flags.add("ONBOARDING_" + campaign.get());
            }
        }

        return Lists.newArrayList(flags);
    }

    private Optional<String> getCampaignFeatureFlags(Optional<DeviceOrigin> origin) {

        if (!origin.isPresent() || Strings.isNullOrEmpty(origin.get().getCampaign())) {
            return Optional.empty();
        }

        final String originCampaign = origin.get().getCampaign();

        for (String campaign : flagsConfiguration.getCampaigns().keySet()) {
            for (String keyWord : flagsConfiguration.getCampaigns().get(campaign)) {
                if (originCampaign.contains(keyWord)) {
                    return Optional.of(campaign);
                }
            }
        }

        return Optional.empty();
    }
}
