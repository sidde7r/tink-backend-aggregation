package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    @Override
    public LocalDateTime unmarshal(String v) {
        return LocalDateTime.parse(v);
    }

    @Override
    public String marshal(LocalDateTime v) {
        return v.toString();
    }
}
