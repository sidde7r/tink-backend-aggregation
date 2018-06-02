package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;

public class TransactionLinkPromptRequest {
    @Tag(1)
    @ApiModelProperty(name = "answer", required = true)
    private String answer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
