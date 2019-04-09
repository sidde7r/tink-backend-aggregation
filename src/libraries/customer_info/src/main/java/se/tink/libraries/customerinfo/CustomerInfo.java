package se.tink.libraries.customerinfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class CustomerInfo {

    private final List<NameElement> nameElements;
    private final LocalDate dateOfBirth;

    protected CustomerInfo(Builder builder) {
        nameElements = ImmutableList.copyOf(builder.nameElements);
        dateOfBirth = builder.dateOfBirth;
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
        CustomerInfo build();
    }

    public abstract static class Builder
            implements FirstNameElementBuilderStep,
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

        public abstract CustomerInfo build();
    }

    @JsonIgnore
    public abstract Map<String, String> toMap();

    @JsonIgnore
    protected Map<String, String> baseMap() {
        Map<String, String> map = new LinkedHashMap<>();

        int elementCount = 1;
        for (NameElement nameElement : nameElements) {
            map.put(
                    "name_" + (elementCount++) + "_" + nameElement.getType(),
                    nameElement.getValue());
        }

        map.put("dateOfBirth", dateOfBirth.toString());

        return map;
    }

    @JsonIgnore
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        nameElements.stream()
                .map(NameElement::getValue)
                .filter(s -> s.trim().length() > 0)
                .forEach(s -> fullName.append(" " + s.trim()));

        return fullName.toString().trim();
    }

    @JsonIgnore
    public abstract String getSsn();

    public List<NameElement> getNameElements() {
        return nameElements;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}
