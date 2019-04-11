package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.CharacterEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.CommonEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class GenericResponse<T> {

    public static final class Any extends GenericResponse<Object> {}

    @JsonProperty("caract")
    private CharacterEntity character;

    @JsonProperty("donnees")
    private T data;

    @JsonProperty("commun")
    private CommonEntity common;

    public T getData() {
        return data;
    }

    public boolean isOk() {
        return common != null && "ok".equalsIgnoreCase(common.getStatus());
    }

    public boolean isEncrypted() {
        return character != null && character.isEncrypted();
    }

    public boolean isCompressed() {
        return character != null && character.isCompressed();
    }
}
