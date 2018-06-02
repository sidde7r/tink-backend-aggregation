package se.tink.backend.common.tracking.intercom;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import io.intercom.api.CustomAttribute;
import io.intercom.api.Event;
import io.intercom.api.Intercom;
import io.intercom.api.NotFoundException;
import io.intercom.api.RateLimitException;
import io.intercom.api.ServerException;
import io.intercom.api.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import se.tink.backend.common.config.IntercomConfiguration;
import se.tink.backend.common.retry.RetryerBuilder;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.TrackableEvent;
import se.tink.backend.common.tracking.intercom.model.DeleteUserRequest;
import se.tink.backend.common.tracking.intercom.model.DeleteUserResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IntercomTracker implements EventTracker {
    private static final String INTERCOM_API = "https://api.intercom.io";
    private static final LogUtils log = new LogUtils(IntercomTracker.class);
    private static final ImmutableMap<String, String> REWRITES = ImmutableMap.<String, String>builder()
            .put("Last updated", "Last updated at")
            .put("First updated", "First updated at")
            .build();

    private static final int CUSTOM_ATTRIBUTE_MAX_LENGTH =  255;

    private final boolean enabled;
    private ApacheHttpClient4 client;
    private IntercomConfiguration configuration;

    @Inject
    public IntercomTracker(IntercomConfiguration configuration) {
        enabled = configuration.isEnabled();

        if (!enabled) {
            return;
        }

        this.client = ApacheHttpClient4.create();
        this.configuration = configuration;

        Intercom.setAppID(configuration.getAppId());
        Intercom.setToken(configuration.getAccessToken());
    }

    public void deleteUser(String userId) throws Exception {
        if (enabled && !Strings.isNullOrEmpty(userId)) {
            delete(userId);
        }
    }

    @Override
    public void trackUserProperties(TrackableEvent trackableEvent) {
        if (!enabled) {
            return;
        }

        User user = find(trackableEvent.getUserId());
        if (user == null) {
            return;
        }

        HashMap<String, Object> properties = Maps.newHashMap(trackableEvent.getProperties());

        if (trackableEvent.getProperties() != null && !properties.isEmpty()) {
            String email = (String) properties.remove(EventTracker.Properties.EMAIL);

            if (!Strings.isNullOrEmpty(email)) {
                user.setEmail(email);
            }

            Date created = (Date) properties.remove(EventTracker.Properties.CREATED);

            if (created != null) {
                user.setSignedUpAt(created.getTime()/1000);
            }

            String name = (String) properties.remove(EventTracker.Properties.NAME);

            if (!Strings.isNullOrEmpty(name)) {
                user.setName(name);
            }

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getKey().startsWith("$") || entry.getValue() == null) {
                    continue;
                }

                String customAttributeName = rewriteCustomAttribute(entry.getKey());

                if (entry.getValue() instanceof String) {
                    String customAttributeValue = (String) entry.getValue();

                    if (customAttributeValue.length() > CUSTOM_ATTRIBUTE_MAX_LENGTH) {
                        log.info(user.getId(), "Too long custom attribute: " + entry.getKey());
                        continue;
                    }

                    user.addCustomAttribute(
                            CustomAttribute.newStringAttribute(customAttributeName, customAttributeValue));
                } else if (entry.getValue() instanceof Double) {
                    user.addCustomAttribute(
                            CustomAttribute.newDoubleAttribute(customAttributeName, (Double) entry.getValue()));
                } else if (entry.getValue() instanceof Boolean) {
                    user.addCustomAttribute(
                            CustomAttribute.newBooleanAttribute(customAttributeName, (Boolean) entry.getValue()));
                } else if (entry.getValue() instanceof Date) {
                    user.addCustomAttribute(CustomAttribute.newLongAttribute(customAttributeName, ((Date) entry.getValue()).getTime()/1000));
                } else if (entry.getValue() instanceof Long) {
                    user.addCustomAttribute(CustomAttribute.newLongAttribute(customAttributeName, (Long) entry.getValue()));
                } else if (entry.getValue() instanceof Float) {
                    user.addCustomAttribute(
                            CustomAttribute.newFloatAttribute(customAttributeName, (Float) entry.getValue()));
                } else if (entry.getValue() instanceof ArrayList) {
                    String customAttributeValue = SerializationUtils.serializeToString(entry.getValue());

                    if (customAttributeValue.length() > CUSTOM_ATTRIBUTE_MAX_LENGTH) {
                        log.info(user.getId(), "Too long custom attribute: " + entry.getKey());
                        continue;
                    }

                    user.addCustomAttribute(
                            CustomAttribute.newStringAttribute(customAttributeName, customAttributeValue));
                } else if (entry.getValue() instanceof Integer) {
                    user.addCustomAttribute(
                            CustomAttribute.newIntegerAttribute(customAttributeName, (Integer) entry.getValue()));
                } else {
                    log.warn(trackableEvent.getUserId(), "Unknown custom attribute type: " + entry.getValue().getClass());
                }
            }
        }

        send(user);
    }

    private String rewriteCustomAttribute(String name) {
        if (REWRITES.containsKey(name)) {
            name = REWRITES.get(name);
        }

        return name.toLowerCase().replace(" ", "_");
    }

    private String rewriteEventName(String name) {
        if (REWRITES.containsKey(name)) {
            name = REWRITES.get(name);
        }

        return name.toLowerCase().replace(" ", "-").replace(".", "-");
    }

    @Override
    public void trackEvent(TrackableEvent trackableEvent) {
        if (!enabled) {
            return;
        }

        final Event event = new Event();
        event.setEventName(rewriteEventName(trackableEvent.getEventType()));
        event.setUserID(trackableEvent.getUserId());

        if (trackableEvent.getProperties() != null && !trackableEvent.getProperties().isEmpty()) {
            Map<String, Object> metadata = Maps.newHashMap();

            for (Map.Entry<String,Object> entry : trackableEvent.getProperties().entrySet()) {
                metadata.put(entry.getKey(), entry.getValue());
            }

            event.setMetadata(metadata);
        }

        send(event);
    }

    private void send(final Event event) {
        Retryer<Void> retryer = RetryerBuilder.<Void>newBuilder(log, "event")
                .retryIfExceptionOfType(RateLimitException.class)
                .retryIfExceptionOfType(ServerException.class)
                .withWaitStrategy(WaitStrategies.fibonacciWait(1000, 1, TimeUnit.MINUTES))
                .withStopStrategy(StopStrategies.stopAfterDelay(2, TimeUnit.MINUTES))
                .build();

        try {
            retryer.call(() -> {
                try {
                    Event.create(event);
                } catch (NotFoundException e) {
                    log.debug(event.getUserID(),
                            String.format("Unable to send event '%s' to Intercom. The user doesn't exist.",
                                    event.getEventName()));
                }
                return null;
            });
        } catch (RetryException e) {
            log.warn(event.getUserID(),
                    String.format("Unable to send event '%s' to Intercom. Exhausted retries.", event.getEventName()),
                    e);
        } catch (ExecutionException e) {
            log.warn(event.getUserID(), String.format("Unable to send event '%s' to Intercom.", event.getEventName()),
                    e);

        }
    }

    private User find(final String userId) {
        Retryer<User> retryer = RetryerBuilder.<User>newBuilder(log, "find-user")
                .retryIfExceptionOfType(RateLimitException.class)
                .retryIfExceptionOfType(ServerException.class)
                .withWaitStrategy(WaitStrategies.fibonacciWait(1000, 1, TimeUnit.MINUTES))
                .withStopStrategy(StopStrategies.stopAfterDelay(2, TimeUnit.MINUTES))
                .build();

        try {
            return retryer.call(() -> {
                try {
                    return User.find(userId);
                } catch (NotFoundException e) {
                    log.debug(userId, "Unable to find user. Returning a new instance.");

                    User user = new User();
                    user.setUserId(userId);
                    return user;
                }
            });
        } catch (RetryException e) {
            log.warn(userId, "Unable to find user. Exhausted retries.", e);
        } catch (ExecutionException e) {
            log.warn(userId, "Unable to find user.", e);
        }

        return null;
    }

    private void send(final User user) {
        Retryer<Void> retryer = RetryerBuilder.<Void>newBuilder(log, "update-user")
                .retryIfExceptionOfType(RateLimitException.class)
                .retryIfExceptionOfType(ServerException.class)
                .withWaitStrategy(WaitStrategies.fibonacciWait(1000, 1, TimeUnit.MINUTES))
                .withStopStrategy(StopStrategies.stopAfterDelay(2, TimeUnit.MINUTES))
                .build();

        try {
            retryer.call(() -> {
                try {
                    User.update(user);
                } catch (NotFoundException e) {
                    log.debug(user.getUserId(), "Unable to update user. The user doesn't exist.");
                }
                return null;
            });
        } catch (RetryException e) {
            log.warn(user.getId(), "Unable to update user. Exhausted retries.", e);
        } catch (ExecutionException e) {
            log.warn(user.getId(), "Unable to update user.", e);
        }
    }

    private void delete(final String userId) throws Exception {
        try {
            Map<String, String> params = Maps.newHashMap();
            params.put("user_id", userId);

            User user = User.find(params);

            // Intercom doesn't expose a way to permanently delete users with their Java SDK so we do it manually
            // according to https://developers.intercom.com/v2.0/reference#delete-users
            DeleteUserRequest request = new DeleteUserRequest();
            request.setIntercomUserId(user.getId());

            DeleteUserResponse response = client.resource(INTERCOM_API + "/user_delete_requests")
                    .header("Authorization", "Bearer " + configuration.getAccessToken())
                    .type(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(DeleteUserResponse.class, request);

            log.info(userId, "User deleted. Intercom request id: " + (response == null ? "N/A" : response.getId()));
        } catch (NotFoundException e) {
            log.info(userId, "User not found.");
        } catch (Exception e) {
            log.error(userId, "Unable to delete user.", e);
            throw e;
        }
    }
}
