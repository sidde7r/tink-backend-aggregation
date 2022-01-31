package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import lombok.Getter;

@Getter
public class SubImageNumberCheckSumCorrelation {

    private final String checkSum;
    private final String number;

    SubImageNumberCheckSumCorrelation(String checkSum, int number) {
        this.checkSum = checkSum;
        this.number = "" + number;
    }
}
