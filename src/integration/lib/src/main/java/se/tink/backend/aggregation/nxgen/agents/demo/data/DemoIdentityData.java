package se.tink.backend.aggregation.nxgen.agents.demo.data;

import java.time.LocalDate;
import java.util.List;
import se.tink.libraries.identitydata.NameElement;

public interface DemoIdentityData {
    List<NameElement> getNameElements();

    LocalDate getDateOfBirth();
}
