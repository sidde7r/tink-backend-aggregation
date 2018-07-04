package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
