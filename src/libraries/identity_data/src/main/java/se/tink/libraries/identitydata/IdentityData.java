package se.tink.libraries.identitydata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;

public class IdentityData {

    private final List<NameElement> nameElements;
    private final LocalDate dateOfBirth;

    protected IdentityData(Builder builder) {
        nameElements = ImmutableList.copyOf(builder.nameElements);
        dateOfBirth = builder.dateOfBirth;
    }

    public static InitialBuilderStep builder() {
        return new Builder();
    }

    public interface InitialBuilderStep {
        DateOfBirthBuilderStep setFullName(String fullname);

        FirstNameElementBuilderStep addFirstNameElement(String val);
    }

    public interface FirstNameElementBuilderStep {
        FirstNameElementBuilderStep addFirstNameElement(String val);

        SurnameElementBuilderStep addSurnameElement(String val);
    }

    public interface SurnameElementBuilderStep {
        SurnameElementBuilderStep addSurnameElement(String val);

        FinalBuilderStep setDateOfBirth(LocalDate val);
    }

    public interface DateOfBirthBuilderStep {
        FinalBuilderStep setDateOfBirth(LocalDate val);
    }

    public interface FinalBuilderStep {
        IdentityData build();
    }

    public static class Builder
            implements InitialBuilderStep,
                    FirstNameElementBuilderStep,
                    SurnameElementBuilderStep,
                    DateOfBirthBuilderStep,
                    FinalBuilderStep {
        private List<NameElement> nameElements;
        private LocalDate dateOfBirth;

        protected Builder() {
            this.nameElements = new ArrayList<>();
        }

        public DateOfBirthBuilderStep setFullName(String val) {
            this.nameElements =
                    Collections.singletonList(new NameElement(NameElement.Type.FULLNAME, val));
            return this;
        }

        public FirstNameElementBuilderStep addFirstNameElement(String val) {
            this.nameElements.add(new NameElement(NameElement.Type.FIRST_NAME, val));
            return this;
        }

        public SurnameElementBuilderStep addSurnameElement(String val) {
            this.nameElements.add(new NameElement(NameElement.Type.SURNAME, val));
            return this;
        }

        public FinalBuilderStep setDateOfBirth(LocalDate val) {
            dateOfBirth = val;
            return this;
        }

        public IdentityData build() {
            return new IdentityData(this);
        }
    }

    /**
     * Shorthand throwing merger/{@link BinaryOperator}&lt;IdentityData&gt; method that can be used
     * as a reducer when building identity data from multiple sources (e.g. multiple credit cards),
     * where only one (definite) IdentityData is desired.
     *
     * <p>Example usage:
     *
     * <pre>
     * return apiClient
     *          .getCards()
     *          .stream()
     *          .map(Card::getHolderName)
     *          .distinct()
     *          .map(name -> SeIdentityData.of(name, ssn))
     *          .reduce(IdentityData::throwingMerger)
     *          .get();
     * </pre>
     *
     * Because of how {@link BinaryOperator} reducers work in Java, a Stream with one element will
     * never invoke the reducer, whereas a Stream with two or more elements of course will. Because
     * of this, this reducer <strong>only throws</strong> when a Stream contains two or more
     * possible identities. It is therefore <strong>imperative</strong> to call <code>distinct()
     * </code> on any data used to build an identity <strong>before</strong> using this as a
     * reducer. See the example above.
     *
     * @return Never returns, always throws an {@link IllegalStateException}
     */
    @JsonIgnore
    public static IdentityData throwingMerger(IdentityData first, IdentityData second) {
        throw new IllegalStateException(
                String.format("Found more than one identity: %s, %s", first, second));
    }

    @JsonIgnore
    public Map<String, String> toMap() {
        return baseMap();
    }

    @JsonIgnore
    protected Map<String, String> baseMap() {
        Map<String, String> map = new LinkedHashMap<>();

        int elementCount = 1;
        for (NameElement nameElement : nameElements) {
            map.put(
                    "name_" + (elementCount++) + "_" + nameElement.getType(),
                    nameElement.getValue());
        }

        if (dateOfBirth != null) {
            map.put("dateOfBirth", dateOfBirth.toString());
        }

        return map;
    }

    @JsonIgnore
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        nameElements.stream()
                .map(NameElement::getValue)
                .filter(s -> !Objects.isNull(s))
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .forEach(s -> fullName.append(" ").append(s));

        return fullName.toString().trim();
    }

    @JsonIgnore
    public String getSsn() {
        return null;
    }

    public List<NameElement> getNameElements() {
        return nameElements;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public boolean equals(final Object obj) {
        if (Objects.isNull(obj)) {
            return false;
        }
        final IdentityData other = (IdentityData) obj;
        if (this == other) {
            return true;
        } else {
            return getFullName().equals(other.getFullName());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getFullName());
    }

    @Override
    public String toString() {
        return "IdentityData{"
                + "nameElements="
                + Joiner.on(",").join(nameElements)
                + ", dateOfBirth="
                + dateOfBirth
                + '}';
    }
}
