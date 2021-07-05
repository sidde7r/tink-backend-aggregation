@XmlSchema(
        xmlns = {
            @XmlNs(prefix = "env", namespaceURI = "http://schemas.xmlsoap.org/soap/envelope/"),
            @XmlNs(prefix = "ns4", namespaceURI = EntitiesConstants.Urls.SOAP_NAMESPACE)
        },
        elementFormDefault = jakarta.xml.bind.annotation.XmlNsForm.QUALIFIED)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(type = LocalDateTime.class, value = LocalDateTimeAdapter.class),
    @XmlJavaTypeAdapter(type = LocalDate.class, value = LocalDateAdapter.class)
})
package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlSchema;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.adapters.LocalDateAdapter;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.adapters.LocalDateTimeAdapter;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.constants.EntitiesConstants;
