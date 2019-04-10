package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils;

import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class EuroInformationDateDeserializer extends XmlAdapter<String, Date> {
    ThreadSafeDateFormat shortDateFormatter =
            ThreadSafeDateFormat.FORMATTER_INTEGER_DATE; // yyyyMMdd
    ThreadSafeDateFormat longDateFormatter = new ThreadSafeDateFormat("yyyyMMddHHmmss");

    @Override
    public Date unmarshal(String v) throws Exception {
        switch (v.length()) {
                // yyyyMMdd
            case 8:
                return shortDateFormatter.parse(v);
                // yyyyMMddHHmmss
            case 14:
                return longDateFormatter.parse(v);
            default:
                throw new IllegalArgumentException(v);
        }
    }

    @Override
    public String marshal(Date v) {
        throw new NotImplementedException("");
    }
}
