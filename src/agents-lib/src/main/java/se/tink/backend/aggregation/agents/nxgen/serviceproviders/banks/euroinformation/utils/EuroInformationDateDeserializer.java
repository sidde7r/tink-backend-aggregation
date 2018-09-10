package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class EuroInformationDateDeserializer extends XmlAdapter<String, Date> {

    DateFormat shortDate = new SimpleDateFormat("yyyyMMdd");
    DateFormat longDate = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public Date unmarshal(String v) throws Exception {
        switch (v.length()) {
        //yyyyMMdd
        case 8:
            return shortDate.parse(v);
        //yyyyMMddHHmmss
        case 14:
            return longDate.parse(v);
        default:
            throw new IllegalArgumentException(v);
        }
    }

    @Override
    public String marshal(Date v) {
        throw new NotImplementedException("");
    }
}
