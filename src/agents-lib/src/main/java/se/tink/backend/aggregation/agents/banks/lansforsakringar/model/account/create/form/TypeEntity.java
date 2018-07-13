package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TypeEntity {

    @JsonProperty("response_type")
    private String responseType;

    private String pickerTitle;

    private List<OptionEntity> options;

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getPickerTitle() {
        return pickerTitle;
    }

    public void setPickerTitle(String pickerTitle) {
        this.pickerTitle = pickerTitle;
    }

    public List<OptionEntity> getOptions() {
        return options;
    }

    public void setOptions(List<OptionEntity> options) {
        this.options = options;
    }

    public boolean isNumeric() {
        return responseType.equalsIgnoreCase(".NumberInput");
    }

    public boolean isMultiResponse() {
        return responseType.equalsIgnoreCase(".MultiResponse");
    }

    public boolean isSingleResponse() {
        return responseType.equalsIgnoreCase(".SingleResponse");
    }

    public boolean isBooleanResponse() {
        return responseType.equalsIgnoreCase(".BoolResponse");
    }

    public boolean isInputLockResponse() {
        return responseType.equalsIgnoreCase(".InputLockResponse");
    }
}
