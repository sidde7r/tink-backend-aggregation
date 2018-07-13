package se.tink.backend.aggregation.agents.banks.icabanken;

import com.google.common.base.Preconditions;

public class ICABankenAccountNumberUtils {
    
    public static boolean isOldFormat(String accountNumber) {
        return accountNumber.matches("[0-9]{4}-[0-9]{3} [0-9]{3} [0-9]");
    }
    
    public static boolean isNewFormat(String accountNumber) {
        return accountNumber.matches("[0-9]{11}");
    }
    
    public static String fromNewFormatToOldFormat(String accountNumber) {
        Preconditions.checkArgument(isNewFormat(accountNumber));
        
        // Splitting the parts of for debugability.
        String partOne = accountNumber.substring(0, 4);
        String partTwo = accountNumber.substring(4, 7);
        String partThree = accountNumber.substring(7, 10);
        String partFour = accountNumber.substring(10, 11);
                
        String result = String.format("%s-%s %s %s", partOne, partTwo, partThree, partFour);
        
        Preconditions.checkState(isOldFormat(result));
        return result;
    }
    
}
