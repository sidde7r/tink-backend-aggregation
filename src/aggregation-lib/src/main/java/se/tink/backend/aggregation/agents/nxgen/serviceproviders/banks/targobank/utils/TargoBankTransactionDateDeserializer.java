package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class TargoBankTransactionDateDeserializer extends XmlAdapter<String, Date> {

    DateFormat f = new SimpleDateFormat("yyyyMMdd");

    @Override
    public Date unmarshal(String v) throws Exception {
        return f.parse(v);
    }

    @Override
    public String marshal(Date v) throws Exception {
        throw new NotImplementedException("");
    }
}
