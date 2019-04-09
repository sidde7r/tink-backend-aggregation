package se.tink.libraries.application;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class GenericApplicationFieldGroup {
    private Map<String, String> fields;
    private String name;
    private List<GenericApplicationFieldGroup> subGroups;

    public void addSubGroup(GenericApplicationFieldGroup group) {
        if (this.subGroups == null) {
            this.subGroups = Lists.newArrayList();
        }

        this.subGroups.add(group);
    }

    public void addSubGroups(List<GenericApplicationFieldGroup> groups) {
        if (this.subGroups == null) {
            this.subGroups = Lists.newArrayList();
        }

        this.subGroups.addAll(groups);
    }

    public String getField(String name) {
        return tryGetField(name).orElse(null);
    }

    public Optional<String> tryGetField(String name) {
        if (this.fields == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(fields.get(name));
    }

    public Boolean getFieldAsBool(String name) {
        return tryGetFieldAsBool(name).orElse(null);
    }

    public Optional<Boolean> tryGetFieldAsBool(String name) {

        Optional<String> value = tryGetField(name);

        if (!value.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(
                Objects.equal(value.get(), ApplicationFieldOptionValues.TRUE)
                        || Objects.equal(value.get(), ApplicationFieldOptionValues.YES));
    }

    public Double getFieldAsDouble(String name) {
        return tryGetFieldAsDouble(name).orElse(null);
    }

    public Optional<Double> tryGetFieldAsDouble(String name) {

        Optional<String> value = tryGetField(name);

        if (!value.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.valueOf(value.get().replace(",", ".")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Integer getFieldAsInteger(String name) {
        return tryGetFieldAsInteger(name).orElse(null);
    }

    public Optional<Integer> tryGetFieldAsInteger(String name) {

        Optional<String> value = tryGetField(name);

        if (!value.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.valueOf(value.get()));
        } catch (NumberFormatException e1) {
            // As fallback, try to parse the value as a double and get the integer from that
            // instead.
            try {
                return Optional.of(Double.valueOf(value.get().replace(",", ".")).intValue());
            } catch (NumberFormatException e2) {
                return Optional.empty();
            }
        }
    }

    public static final TypeReference<List<String>> LIST_OF_STRINGS =
            new TypeReference<List<String>>() {};

    public List<String> getFieldAsListOfStrings(String name) {
        return SerializationUtils.deserializeFromString(getField(name), LIST_OF_STRINGS);
    }

    public static final TypeReference<List<List<Point>>> LIST_OF_LIST_OF_POINTS =
            new TypeReference<List<List<Point>>>() {};

    public List<List<Point>> getFieldAsListOfListOfPoints(String name) {
        return SerializationUtils.deserializeFromString(getField(name), LIST_OF_LIST_OF_POINTS);
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }

    public List<GenericApplicationFieldGroup> getSubGroups() {
        return subGroups;
    }

    public void putField(String name, String value) {
        if (this.fields == null) {
            this.fields = Maps.newHashMap();
        }

        this.fields.put(name, value);
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubGroups(List<GenericApplicationFieldGroup> groups) {
        this.subGroups = groups;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("name", name)
                .add("fields", fields)
                .add("subGroups", subGroups)
                .toString();
    }
}
