package se.tink.backend.common.utils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.Date;
import java.util.Map;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.rpc.UserPropertiesBuilderCommand;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.core.UserState;

public class AbnArmoUserPropertiesBuilder extends UserPropertiesBuilder {
    @Inject
    public AbnArmoUserPropertiesBuilder(
            CredentialsRepository credentialsRepository,
            UserStateRepository userStateRepository,
            UserDemographicsRepository userDemographicsRepository) {
        super(credentialsRepository, userStateRepository, userDemographicsRepository);
    }

    @Override
    public Map<String, Object> build(UserPropertiesBuilderCommand command) {
        final Map<String, Object> properties = Maps.newHashMap();

        populateClientProperties(properties, command.getUserAgent());
        populateUserProperties(properties, command.getUser());
        populateUserDemographicsProperties(properties, command.getUser().getId());

        return properties;
    }

    @Override
    protected void populateUserDemographicsProperties(Map<String, Object> properties, String userId) {
        UserDemographics userDemographics = userDemographicsRepository.findOne(userId);
        UserState userState = userStateRepository.findOne(userId);

        if (userDemographics != null && userState != null) {
            setProperty(properties, "Number of transactions", userDemographics.getTransactionCount());
            setProperty(properties, "Number of periods", userDemographics.getValidCleanDataPeriodsCount());

            if (userState.getInitialAmountCategorizationLevel() != null) {
                int level = userState.getInitialAmountCategorizationLevel().intValue();
                setProperty(properties, "Initial categorization level (amount)", level);
            }

            if (userState.getAmountCategorizationLevel() != null) {
                int level = userState.getAmountCategorizationLevel().intValue();
                setProperty(properties, "Categorization level (amount)", level);
            }

            setProperty(properties, "First updated", userDemographics.getFirstUpdatedEvent());
            setProperty(properties, "Last updated", userDemographics.getLastUpdatedEvent());
        }
    }

    private void setProperty(final Map<String, Object> properties, String key, Number value) {
        int intValue = (value != null) ? value.intValue() : 0;
        properties.put(key, intValue);
    }

    private void setProperty(final Map<String, Object> properties, String key, Date value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

}
