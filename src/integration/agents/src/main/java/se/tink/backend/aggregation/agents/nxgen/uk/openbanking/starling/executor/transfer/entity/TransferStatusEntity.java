package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity;

import org.apache.commons.httpclient.HttpStatus;

public class TransferStatusEntity {

    private int code;
    private String body;

    private TransferStatusEntity(int code, String body) {
        this.code = code;
        this.body = body;
    }

    public boolean isOk() {
        return code == HttpStatus.SC_OK;
    }

    public String getBody() {
        return body;
    }

    public static TransferStatusEntity ok() {
        return new TransferStatusEntity(HttpStatus.SC_OK, "");
    }

    public static TransferStatusEntity fail(int code, String body) {
        return new TransferStatusEntity(code, body);
    }
}
