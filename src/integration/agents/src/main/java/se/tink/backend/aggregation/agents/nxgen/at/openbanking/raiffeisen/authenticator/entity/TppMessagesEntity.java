package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppMessagesEntity {

    private String code;

    private String text;

    public String getErrorText() {
        return (!Strings.isNullOrEmpty(text)) ? text : code;
    }
}
