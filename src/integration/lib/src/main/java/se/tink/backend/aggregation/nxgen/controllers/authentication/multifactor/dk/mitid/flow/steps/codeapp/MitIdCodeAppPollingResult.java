package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp;

public enum MitIdCodeAppPollingResult {
    POLLING,
    OK,
    EXPIRED,
    REJECTED,
    TECHNICAL_ERROR,
    UNKNOWN
}
