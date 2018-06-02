package se.tink.backend.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.rpc.UserPropertiesBuilderCommand;
import se.tink.backend.core.*;
import se.tink.backend.utils.LogUtils;

public abstract class UserPropertiesBuilder {
    protected static final Set<CredentialsStatus> INVALID_CREDENTIALS_STATUSES = Sets.newHashSet(
            CredentialsStatus.AUTHENTICATION_ERROR, CredentialsStatus.TEMPORARY_ERROR,
            CredentialsStatus.PERMANENT_ERROR);
    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final LogUtils log = new LogUtils(UserPropertiesBuilder.class);

    protected final CredentialsRepository credentialsRepository;
    protected final UserStateRepository userStateRepository;
    protected final UserDemographicsRepository userDemographicsRepository;

    public UserPropertiesBuilder(
            CredentialsRepository credentialsRepository,
            UserStateRepository userStateRepository,
            UserDemographicsRepository userDemographicsRepository) {
        this.credentialsRepository = credentialsRepository;
        this.userStateRepository = userStateRepository;
        this.userDemographicsRepository = userDemographicsRepository;
    }

    public abstract Map<String, Object> build(UserPropertiesBuilderCommand command);

    protected void populateCategorizationProperties(final Map<String, Object> properties, String userId) {
        UserState userState = userStateRepository.findOne(userId);

        if (userState != null) {
            if (userState.getInitialAmountCategorizationLevel() != null) {
                int level = userState.getInitialAmountCategorizationLevel().intValue();
                properties.put("Initial categorization level (amount)", level);
            }

            if (userState.getInitialExpensesLessThan10kCategorizationLevel() != null) {
                int level = userState.getInitialExpensesLessThan10kCategorizationLevel().intValue();
                properties.put("Initial categorization level (amount, every-day transactions)", level);
            }

            if (userState.getAmountCategorizationLevel() != null) {
                int level = userState.getAmountCategorizationLevel().intValue();
                properties.put("Categorization level (amount)", level);
            }

            if (userState.getExpensesLessThan10kCategorizationLevel() != null) {
                int level = userState.getExpensesLessThan10kCategorizationLevel().intValue();
                properties.put("Categorization level (amount, every-day transactions)", level);
            }
        }
    }

    protected void populateClientProperties(final Map<String, Object> properties, String userAgent) {
        properties.put("User agent", userAgent);
    }

    protected void populateCredentialsProperties(final Map<String, Object> properties, final User user) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        List<String> validProviderNames = Lists.newArrayList();
        List<String> invalidProviderNames = Lists.newArrayList();

        for (Credentials c : credentials) {
            if (INVALID_CREDENTIALS_STATUSES.contains(c.getStatus())) {
                invalidProviderNames.add(c.getProviderName());
            } else {
                validProviderNames.add(c.getProviderName());
            }
        }

        if (validProviderNames.size() == 0) {
            validProviderNames.add("none");
        }

        if (invalidProviderNames.size() == 0) {
            invalidProviderNames.add("none");
        }

        properties.put("Providers (valid)", validProviderNames);
        properties.put("Providers (invalid)", invalidProviderNames);
    }

    protected void populateUserDemographicsProperties(final Map<String, Object> properties, String userId) {
        UserDemographics userDemographics = userDemographicsRepository.findOne(userId);

        if (userDemographics != null) {
            properties.putAll(userDemographics.toProperties());
        }
    }

    protected void populateUserOriginProperties(final Map<String, Object> properties, final UserOrigin userOrigin) {
        properties.put("Origin Media Source", userOrigin.getMediaSource());
        properties.put("Origin Campaign", userOrigin.getCampaign());
        properties.put("Origin Device Type", userOrigin.getDeviceType());
        properties.put("Origin Organic", userOrigin.isOrganic());

        if (userOrigin.isFacebook()) {
            properties.put("Origin Facebook Ad Group Name", userOrigin.getFbAdGroupName());
            properties.put("Origin Facebook Ad Set Name", userOrigin.getFbAdSetName());
        }
    }

    protected void populateUserProperties(final Map<String, Object> properties, final User user) {
        properties.put("Locale", user.getProfile().getLocale());
        properties.put("Market", user.getProfile().getMarket());
        properties.put("Id", user.getId());

        try {
            properties.put("Feature flags", mapper.writeValueAsString(user.getFlags()));
        } catch (JsonProcessingException e) {
            log.error(user.getId(), "Could not serialize flags", e);
        }
    }
}
