package se.tink.libraries.giro.validation;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class LuhnCheck {
    public static boolean isLastCharCorrectLuhnMod10Check(String numericString) {
        Preconditions.checkArgument(StringUtils.isNumeric(numericString));

        String lastCharacter = numericString.substring(numericString.length() - 1);
        String otherCharacters = numericString.substring(0, numericString.length() - 1);

        if (otherCharacters.isEmpty()) {
            return false;
        }

        int lastDigit = Integer.parseInt(lastCharacter);
        int calculatedCheck = calculateLuhnMod10Check(otherCharacters);

        return Objects.equal(lastDigit, calculatedCheck);
    }

    public static int calculateLuhnMod10Check(String numericString) {
        Preconditions.checkArgument(StringUtils.isNumeric(numericString));

        List<Integer> digits = Lists.newArrayList();

        char[] numberCharacters = numericString.toCharArray();
        for (Character numberCharacter : numberCharacters) {
            int digit = Integer.parseInt(numberCharacter.toString());
            digits.add(digit);
        }

        return calculateLuhnMod10Check(digits);
    }

    /**
     * Inspired directly from Hibernate validation {@link
     * org.hibernate.validator.internal.util.ModUtil}
     */
    public static int calculateLuhnMod10Check(List<Integer> digits) {
        int sum = 0;
        boolean even = true;

        for (int index = digits.size() - 1; index >= 0; --index) {
            int digit = digits.get(index);
            if (even) {
                digit <<= 1;
            }

            if (digit > 9) {
                digit -= 9;
            }

            sum += digit;
            even = !even;
        }

        // Without doing an additional `% 10` on the statement, we would get value `10` back when
        // `sum % 10 == 0`
        return (10 - sum % 10) % 10;
    }
}
