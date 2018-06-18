package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class TargoBankMsgDateDeserializer extends XmlAdapter<String, Date> {

    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");

    @Override
    public Date unmarshal(String v) throws Exception {
        return dateFormat.parse(v);
    }

    @Override
    public String marshal(Date v) throws Exception {
        throw new NotImplementedException("");
    }
}
