package se.tink.backend.aggregation.agents.agentplatform.authentication;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Ignore;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Ignore
public class TestPersistedDataObject implements Serializable {

    private final String stringParam;

    private final Integer integerValue;
}
