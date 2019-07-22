package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ComponentEntity {

    private List<ComponentsEntity> components;
    private boolean inset;
    private String type;

    public List<ComponentsEntity> getComponents() {
        return components;
    }

    public Optional<ComponentsEntity> getComponentWithForm() {
        return getComponents().stream()
                .filter(component -> component.getFormValue() != null)
                .findFirst();
    }
}
