package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdDemoPasswordField {

    public static Field build() {
        return Field.builder()
                .name("nemIdDemoPasswordField")
                .description("Please enter your NemID password")
                .helpText(
                        "Please enter your password to NemID. It can be a 4 digit password or password with at least 6 alphanumeric characters. If you do not remember it, you can reset your password on NemID website.")
                .minLength(4)
                .maxLength(40)
                .pattern("^(\\d{4}|[a-zA-Z0-9{}!#\"$'%^&,*()_+\\-=:;?.@]{6,40})$")
                .patternError(
                        "Must be between 4 and 40 characters\nMay not begin or end with a blank character\nMay not contain certain special characters, such as æ, ø, å")
                .build();
    }
}
