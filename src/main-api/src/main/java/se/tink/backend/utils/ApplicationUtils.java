package se.tink.backend.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;

public class ApplicationUtils {

    private static final TypeReference<Map<String, String>> TYPE_REFERENCE_MAP_STRING_STRING = new TypeReference<Map<String, String>>() {
    };

    public static Optional<GenericApplicationFieldGroup> getApplicant(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {

        return getApplicant(fieldGroupByName, 0);
    }

    public static Optional<GenericApplicationFieldGroup> getCoApplicant(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {

        return getApplicant(fieldGroupByName, 1);
    }

    public static Optional<GenericApplicationFieldGroup> getApplicant(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName, int index) {

        Optional<GenericApplicationFieldGroup> group = getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.APPLICANTS);

        if (!group.isPresent()) {
            return Optional.empty();
        }

        return getSubGroup(group.get(), index);
    }

    public static int getNumberOfApplicants(ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> applicants = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.APPLICANTS);

        if (!applicants.isPresent() || applicants.get().getSubGroups() == null) {
            return 0;
        }

        return applicants.get().getSubGroups().size();
    }

    public static Optional<GenericApplicationFieldGroup> getFirst(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName, String groupName) {
        List<GenericApplicationFieldGroup> applicantFieldGroups = fieldGroupByName.get(groupName);

        if (applicantFieldGroups.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(applicantFieldGroups.get(0));
    }

    public static Optional<GenericApplicationFieldGroup> getSubGroup(GenericApplicationFieldGroup group, int index) {
        if (index < 0 || index >= group.getSubGroups().size()) {
            return Optional.empty();
        }

        return Optional.of(group.getSubGroups().get(index));
    }

    public static Optional<GenericApplicationFieldGroup> getFirstSubGroup(Optional<GenericApplicationFieldGroup> group, String groupName) {
        if (!group.isPresent()){
            return Optional.empty();
        }
        return getFirstSubGroup(group.get(), groupName);
    }

    public static Optional<GenericApplicationFieldGroup> getFirstSubGroup(GenericApplicationFieldGroup group, String groupName) {
        List<GenericApplicationFieldGroup> groups = getSubGroups(group, groupName);
        if (groups.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(groups.get(0));
    }

    public static List<GenericApplicationFieldGroup> getSubGroups(Optional<GenericApplicationFieldGroup> group,
            String name) {
        if (!group.isPresent()) {
            return Lists.newArrayList();
        }

        return getSubGroups(group.get(), name);
    }

    public static List<GenericApplicationFieldGroup> getSubGroups(GenericApplicationFieldGroup group, String name) {
        if (group == null || group.getSubGroups() == null || group.getSubGroups().isEmpty()) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(Iterables.filter(group.getSubGroups(), Predicates.fieldGroupByName(name)));
    }

    public static ImmutableListMultimap<String, GenericApplicationFieldGroup> getGroupsByName(
            GenericApplication application) {
        return FluentIterable
                .from(application.getFieldGroups())
                .index(GenericApplicationFieldGroup::getName);
    }
}
