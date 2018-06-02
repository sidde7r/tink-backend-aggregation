package se.tink.backend.core.application;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.core.enums.ApplicationStatusKey;

@JsonAutoDetect( // To have getters/setters with any name/type regardless of serialization (e.g. for returning Optional)
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ApplicationState {
    private ApplicationStatusKey newApplicationStatus;
    private HashMap<ApplicationPropertyKey, Object> applicationProperties;

    public ApplicationState() {
        this.applicationProperties = Maps.newHashMap();
    }

    /**
     * If you want to specifically set the status of an application to something else than it was before, use this
     * construct.
     *
     * @param newApplicationStatus The new application status of the application
     */
    public ApplicationState(ApplicationStatusKey newApplicationStatus) {
        this.newApplicationStatus = newApplicationStatus;
        this.applicationProperties = Maps.newHashMap();
    }

    public ApplicationState(ApplicationStatusKey newApplicationStatus,
            Map<ApplicationPropertyKey, Object> applicationProperties) {
        this.newApplicationStatus = newApplicationStatus;
        this.applicationProperties = Maps.newHashMap(applicationProperties);
    }

    /**
     * It's optional to update Application with new status. If no application status sent we should use the previous.
     */
    public Optional<ApplicationStatusKey> getNewApplicationStatus() {
        return Optional.ofNullable(newApplicationStatus);
    }

    public void setNewApplicationStatus(ApplicationStatusKey newApplicationStatus) {
        this.newApplicationStatus = newApplicationStatus;
    }

    public Map<ApplicationPropertyKey, Object> getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(Map<ApplicationPropertyKey, Object> applicationProperties) {
        if (applicationProperties == null) {
            this.applicationProperties = Maps.newHashMap();
        } else {
            this.applicationProperties = Maps.newHashMap(applicationProperties);
        }
    }

    public void setApplicationProperty(ApplicationPropertyKey key, Object value) {
        Preconditions.checkNotNull(key);
        applicationProperties.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationState that = (ApplicationState) o;

        return Objects.equal(this.newApplicationStatus, that.newApplicationStatus) &&
                Objects.equal(this.applicationProperties, that.applicationProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(newApplicationStatus, applicationProperties);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("newApplicationStatus", newApplicationStatus)
                .add("applicationProperties", applicationProperties)
                .toString();
    }
}
