package se.tink.backend.common.i18n;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.common.SwedishTimeRule;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.enums.Gender;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class SocialSecurityNumberTest {

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    private static final String pnrInvalid1 = "1999091";
    private static final String pnrInvalid2 = "19991";
    private static final String pnrInvalid3 = null;
    private static final String pnrInvalid4 = "";
    private static final String pnrInvalid5 = "9999909121204";
    private static final String pnrInvalid6 = "abcdefghijkl";
    private static final String pnrInvalid7 = "195575920357";
    private static final String pnrInvalid8 = "195511310357";

    private static final String pnrFemale1999 = "199909121205";
    private static final String pnrFemale2010 = "201009121227";
    private static final String pnrFemale1900 = "190009121245";
    private static final String pnrFemale1957 = "195709121262";
    private static final String pnrFemale1921 = "192109121281";
    private static final String pnrFemale2000 = "200009121229";

    private static final String pnrMale1999 = "199909121213";
    private static final String pnrMale2010 = "201009121235";
    private static final String pnrMale1900 = "190009121252";
    private static final String pnrMale1957 = "195709121270";
    private static final String pnrMale1921 = "192109121299";
    private static final String pnrMale2000 = "200009121211";

    @Test
    public void validatesCheckSum() {
        assertThat(new SocialSecurityNumber.Sweden("199909121210").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121211").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121212").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121213").isValid()).isTrue();
        assertThat(new SocialSecurityNumber.Sweden("199909121214").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121215").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121216").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121217").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121218").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("199909121219").isValid()).isFalse();

        assertThat(new SocialSecurityNumber.Sweden("200009121220").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121221").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121222").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121223").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121224").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121225").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121226").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121227").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121228").isValid()).isFalse();
        assertThat(new SocialSecurityNumber.Sweden("200009121229").isValid()).isTrue();
    }

    @Test
    public void doesNotValidateCheckSumForDemoUser() {
        String demoUserWithInvalidSSN = "201212121213";

        SocialSecurityNumber.Sweden swedishSSN = new SocialSecurityNumber.Sweden(demoUserWithInvalidSSN);

        try {
            assertThat(swedishSSN.isValid()).isTrue();
        } catch (ComparisonFailure comparisonFailure) {
            throw new ComparisonFailure("Is this not a demo user any more in DemoUser.java? " + demoUserWithInvalidSSN,
                    comparisonFailure.getExpected(), comparisonFailure.getActual());
        }
    }

    @Test
    public void testGenderFemale() throws Exception {
        Gender gender = new SocialSecurityNumber.Sweden(pnrFemale1999).getGender();
        Assert.assertEquals(gender, Gender.FEMALE);

        gender = new SocialSecurityNumber.Sweden(pnrFemale2010).getGender();
        Assert.assertEquals(gender, Gender.FEMALE);

        gender = new SocialSecurityNumber.Sweden(pnrFemale1900).getGender();
        Assert.assertEquals(gender, Gender.FEMALE);

        gender = new SocialSecurityNumber.Sweden(pnrFemale1957).getGender();
        Assert.assertEquals(gender, Gender.FEMALE);

        gender = new SocialSecurityNumber.Sweden(pnrFemale1921).getGender();
        Assert.assertEquals(gender, Gender.FEMALE);

        gender = new SocialSecurityNumber.Sweden(pnrFemale2000).getGender();
        Assert.assertEquals(gender, Gender.FEMALE);
    }

    @Test
    public void testGenderMale() throws Exception {
        Gender gender = new SocialSecurityNumber.Sweden(pnrMale1999).getGender();
        Assert.assertEquals(gender, Gender.MALE);

        gender = new SocialSecurityNumber.Sweden(pnrMale2010).getGender();
        Assert.assertEquals(gender, Gender.MALE);

        gender = new SocialSecurityNumber.Sweden(pnrMale1900).getGender();
        Assert.assertEquals(gender, Gender.MALE);

        gender = new SocialSecurityNumber.Sweden(pnrMale1957).getGender();
        Assert.assertEquals(gender, Gender.MALE);

        gender = new SocialSecurityNumber.Sweden(pnrMale1921).getGender();
        Assert.assertEquals(gender, Gender.MALE);

        gender = new SocialSecurityNumber.Sweden(pnrMale2000).getGender();
        Assert.assertEquals(gender, Gender.MALE);
    }

    @Test
    public void testGenderReturnsNullIfInputInvalid() throws Exception {
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid1).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid2).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid3).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid4).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid5).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid6).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid7).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid8).getGender();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testBirthFemale() throws Exception {
        String birth = new SocialSecurityNumber.Sweden(pnrFemale1999).getBirth();
        Assert.assertEquals(birth, "1999-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrFemale2010).getBirth();
        Assert.assertEquals(birth, "2010-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrFemale1900).getBirth();
        Assert.assertEquals(birth, "1900-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrFemale1957).getBirth();
        Assert.assertEquals(birth, "1957-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrFemale1921).getBirth();
        Assert.assertEquals(birth, "1921-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrFemale2000).getBirth();
        Assert.assertEquals(birth, "2000-09-12");
    }

    @Test
    public void testBirthMale() throws Exception {
        String birth = new SocialSecurityNumber.Sweden(pnrMale1999).getBirth();
        Assert.assertEquals(birth, "1999-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrMale2010).getBirth();
        Assert.assertEquals(birth, "2010-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrMale1900).getBirth();
        Assert.assertEquals(birth, "1900-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrMale1957).getBirth();
        Assert.assertEquals(birth, "1957-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrMale1921).getBirth();
        Assert.assertEquals(birth, "1921-09-12");

        birth = new SocialSecurityNumber.Sweden(pnrMale2000).getBirth();
        Assert.assertEquals(birth, "2000-09-12");
    }

    @Test
    public void testIsInvalid() throws Exception {
        SocialSecurityNumber.Sweden n = new SocialSecurityNumber.Sweden(pnrInvalid1);
        assertFalse(n.isValid());

        n = new SocialSecurityNumber.Sweden(pnrInvalid2);
        assertFalse(n.isValid());

        n = new SocialSecurityNumber.Sweden(pnrInvalid3);
        assertFalse(n.isValid());

        n = new SocialSecurityNumber.Sweden(pnrInvalid4);
        assertFalse(n.isValid());

        n = new SocialSecurityNumber.Sweden(pnrInvalid5);
        assertFalse(n.isValid());

        n = new SocialSecurityNumber.Sweden(pnrInvalid6);
        assertFalse(n.isValid());

        n = new SocialSecurityNumber.Sweden(pnrInvalid7);
        assertFalse(n.isValid());

        n = new SocialSecurityNumber.Sweden(pnrInvalid8);
        assertFalse(n.isValid());
    }

    @Test
    public void testBirthYearFemale() throws Exception {
        int year = new SocialSecurityNumber.Sweden(pnrFemale1999).getBirthYear();
        Assert.assertEquals(year, 1999);

        year = new SocialSecurityNumber.Sweden(pnrFemale2010).getBirthYear();
        Assert.assertEquals(year, 2010);

        year = new SocialSecurityNumber.Sweden(pnrFemale1900).getBirthYear();
        Assert.assertEquals(year, 1900);

        year = new SocialSecurityNumber.Sweden(pnrFemale1957).getBirthYear();
        Assert.assertEquals(year, 1957);

        year = new SocialSecurityNumber.Sweden(pnrFemale1921).getBirthYear();
        Assert.assertEquals(year, 1921);

        year = new SocialSecurityNumber.Sweden(pnrFemale2000).getBirthYear();
        Assert.assertEquals(year, 2000);
    }

    @Test
    public void testBirthYearMale() throws Exception {
        int year = new SocialSecurityNumber.Sweden(pnrMale1999).getBirthYear();
        Assert.assertEquals(year, 1999);

        year = new SocialSecurityNumber.Sweden(pnrMale2010).getBirthYear();
        Assert.assertEquals(year, 2010);

        year = new SocialSecurityNumber.Sweden(pnrMale1900).getBirthYear();
        Assert.assertEquals(year, 1900);

        year = new SocialSecurityNumber.Sweden(pnrMale1957).getBirthYear();
        Assert.assertEquals(year, 1957);

        year = new SocialSecurityNumber.Sweden(pnrMale1921).getBirthYear();
        Assert.assertEquals(year, 1921);

        year = new SocialSecurityNumber.Sweden(pnrMale2000).getBirthYear();
        Assert.assertEquals(year, 2000);
    }

    @Test
    public void testBirthYearReturnsNullIfInputInvalid() throws Exception {
        try {
            new SocialSecurityNumber.Sweden(pnrInvalid1).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }

        try {
            new SocialSecurityNumber.Sweden(pnrInvalid2).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }

        try {
            new SocialSecurityNumber.Sweden(pnrInvalid3).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }

        try {
            new SocialSecurityNumber.Sweden(pnrInvalid4).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }

        try {
            new SocialSecurityNumber.Sweden(pnrInvalid5).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }

        try {
            new SocialSecurityNumber.Sweden(pnrInvalid6).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }

        try {
            new SocialSecurityNumber.Sweden(pnrInvalid7).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }

        try {
            new SocialSecurityNumber.Sweden(pnrInvalid8).getBirthYear();
            fail("Should have been validated");
        } catch (RuntimeException e) {
        }
    }
    
    @Test
    public void testFindInValidPersonnumerCredentials() {
        List<Credentials> credentials = Lists.newArrayList();
        
        Credentials credentialsInvalid = new Credentials();
        credentialsInvalid.setUsername(pnrInvalid1);
        credentialsInvalid.setStatus(CredentialsStatus.UPDATED);
        
        credentials.add(credentialsInvalid);
        
        SocialSecurityNumber.Sweden personNumber = SocialSecurityNumber.Sweden.findPersonNumberFromCredentials(credentials);
        
        Assert.assertNull(personNumber);
    }
    
    @Test
    public void testFindValidPersonnumerCredentials() {
        List<Credentials> credentials = Lists.newArrayList();
        
        Credentials credentialsValid = new Credentials();
        credentialsValid.setUsername(pnrMale1957);
        credentialsValid.setStatus(CredentialsStatus.UPDATED);
        
        credentials.add(credentialsValid);
        
        SocialSecurityNumber.Sweden personNumber = SocialSecurityNumber.Sweden.findPersonNumberFromCredentials(credentials);
        
        Assert.assertNotNull(personNumber);
    }
    
    @Test
    public void testFindValidPersonnumerCredentialsFromList() {
        List<Credentials> credentials = Lists.newArrayList();
        
        Credentials credentialsValid = new Credentials();
        credentialsValid.setUsername(pnrMale1957);
        credentialsValid.setStatus(CredentialsStatus.UPDATED);
        
        Credentials credentialsInValid = new Credentials();
        credentialsInValid.setUserId(pnrInvalid2);
        credentialsValid.setStatus(CredentialsStatus.UPDATED);
        
        credentials.add(credentialsValid);
        credentials.add(credentialsInValid);        
        
        SocialSecurityNumber.Sweden personNumber = SocialSecurityNumber.Sweden.findPersonNumberFromCredentials(credentials);
        
        Assert.assertNotNull(personNumber);
    }
    
    @Test
    public void testFindValidPersonnumerEmptyFromList() {
        List<Credentials> credentials = Lists.newArrayList();
        SocialSecurityNumber.Sweden personNumber = SocialSecurityNumber.Sweden.findPersonNumberFromCredentials(credentials);
        
        Assert.assertNull(personNumber);
    }
    
    @Test
    public void testFindValidPersonnumerFromNull() {
        SocialSecurityNumber.Sweden personNumber = SocialSecurityNumber.Sweden.findPersonNumberFromCredentials(null);
        
        Assert.assertNull(personNumber);
    }

    @Test
    public void testGetBirthDate() {
        SocialSecurityNumber.Sweden personNumber = new SocialSecurityNumber.Sweden("201212121212");

        LocalDate birthDate = personNumber.getBirthDate().toInstant().atZone(ZoneId.of("CET")).toLocalDate();
        assertThat(birthDate.getYear()).isEqualTo(2012);
        assertThat(birthDate.getMonthValue()).isEqualTo(12);
        assertThat(birthDate.getDayOfMonth()).isEqualTo(12);
    }

    @Test
    public void testGetAge_WhenBeforeBirthDay() {
        SocialSecurityNumber.Sweden personNumber = new SocialSecurityNumber.Sweden("201212121212");

        LocalDate now = LocalDate.of(2016, 12, 11);
        int age = personNumber.getAge(now);
        assertThat(age).isEqualTo(3);
    }

    @Test
    public void testGetAge_WhenExactlyOnBirthDay() {
        SocialSecurityNumber.Sweden personNumber = new SocialSecurityNumber.Sweden("201212121212");

        LocalDate now = LocalDate.of(2016, 12, 12);
        int age = personNumber.getAge(now);
        assertThat(age).isEqualTo(4);
    }

    @Test
    public void testGetAge_WhenAfterBirthDay() {
        SocialSecurityNumber.Sweden personNumber = new SocialSecurityNumber.Sweden("201212121212");

        LocalDate now = LocalDate.of(2016, 12, 13);
        int age = personNumber.getAge(now);
        assertThat(age).isEqualTo(4);
    }

    @Test
    public void testAsStringMethods() {
        SocialSecurityNumber.Sweden first = new SocialSecurityNumber.Sweden("19860701-5537");
        SocialSecurityNumber.Sweden second = new SocialSecurityNumber.Sweden("198607015537");

        assertThat(first.asStringWithDash())
                .isEqualTo(second.asStringWithDash())
                .isEqualTo("19860701-5537");
        assertThat(first.asString())
                .isEqualTo(second.asString())
                .isEqualTo("198607015537");
    }
}
