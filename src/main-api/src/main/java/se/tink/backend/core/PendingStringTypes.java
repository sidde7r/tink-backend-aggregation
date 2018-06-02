/**
 * 
 */
package se.tink.backend.core;

/**
 * Descriptions from the bank indicating this it is a non-settled transaction
 */
public enum PendingStringTypes {

    HANDELSBANKEN("PREL. KORTKÖP"),
    
    LANSFORSAKRINGAR("PREL KORTKÖP"),
    
    SWEDBANK("SKYDDAT BELOPP"),

    SWEDBANK_PENDING_TRANSFER("ÖVF VIA INTERNET");
    
    private String value;
    
    private PendingStringTypes(String value) {
        this.setValue(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
