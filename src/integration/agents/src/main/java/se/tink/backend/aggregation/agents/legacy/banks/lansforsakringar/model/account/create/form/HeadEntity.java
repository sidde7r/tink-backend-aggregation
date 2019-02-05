package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeadEntity {

    private String infoText;
    private String acknowledgeText;

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public String getAcknowledgeText() {
        return acknowledgeText;
    }

    public void setAcknowledgeText(String acknowledgeText) {
        this.acknowledgeText = acknowledgeText;
    }
}
