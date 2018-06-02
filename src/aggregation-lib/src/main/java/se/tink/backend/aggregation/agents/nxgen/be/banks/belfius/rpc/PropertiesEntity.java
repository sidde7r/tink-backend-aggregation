package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.ContentListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.BeneficiariesContacts;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PropertiesEntity {
    private String text;
    private ContentListEntity contentList;


    public String getText() {
        return text;
    }

    public boolean cardReaderAllowed(){
        return text.equals("Y");
    }

    public String getChallenge(){
        if(text == null)
            return "";
        return text;}

    public boolean signOk(){
        return text.equals("Y");
    }

    public String getSignType(){
        return contentList == null ? "" : contentList.getSignType();
    }

    public ArrayList<BeneficiariesContacts> getBeneficiaries(){
        return contentList == null ? new ArrayList<>() : contentList.getBeneficiaries();
    }

}
