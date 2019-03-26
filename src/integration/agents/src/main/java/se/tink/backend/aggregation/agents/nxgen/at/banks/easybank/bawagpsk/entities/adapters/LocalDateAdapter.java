package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.adapters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    @Override
    public LocalDate unmarshal(String v) {
        return LocalDate.parse(v, DateTimeFormatter.ISO_OFFSET_DATE);
    }

    @Override
    public String marshal(LocalDate v) {
        return v.toString();
    }
}
