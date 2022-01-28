package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields;

import static org.apache.commons.lang3.StringUtils.repeat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MitIdDemoCprField {

    public static Field build() {
        return Field.builder()
                .name("mitIdDemoCprField")
                .description("Please enter your CPR number")
                .helpText(
                        "CPR number is usually required only during the very first MitID login. If you're continuously asked for it, you can authenticate on your bank's website, enter CPR and select a checkbox to remember it for all future authentications.")
                .minLength(10)
                .maxLength(10)
                .numeric(true)
                .hint(repeat("N", 10))
                .pattern("^\\d{10}$")
                .patternError("CPR must be a 10 digit number")
                .build();
    }
}
