@XmlSchema(
        xmlns = {
            @XmlNs(prefix = "env", namespaceURI = "http://schemas.xmlsoap.org/soap/envelope/"),
            @XmlNs(prefix = "ns4", namespaceURI = BawagPskConstants.Urls.SOAP_NAMESPACE)
        },
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(type = LocalDateTime.class, value = LocalDateTimeAdapter.class),
    @XmlJavaTypeAdapter(type = LocalDate.class, value = LocalDateAdapter.class)
})
package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.adapters.LocalDateAdapter;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.adapters.LocalDateTimeAdapter;
