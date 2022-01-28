package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdDemoUserIdField {

    public static Field build() {
        return Field.builder()
                .name("nemIdUserIdField")
                .description("Please enter your NemID user ID")
                .helpText(
                        "Please enter the same User ID as you would enter to NemID. It’s either NemID number, CPR number or self-chosen NemID username.")
                .minLength(5)
                .maxLength(48)
                .pattern("^[a-zA-Z0-9{}!#\"$'%^&,*()_+\\-=:;?.@]{5,48}$")
                .patternError(
                        "Must be between 5 and 48 characters\nMay not contain certain special characters, such as æ, ø, å\nMay not begin or end with a blank character")
                .build();
    }
}
