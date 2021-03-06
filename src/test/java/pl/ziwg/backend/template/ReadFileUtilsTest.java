package pl.ziwg.backend.template;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadFileUtilsTest {
    private ReadFileUtils readFileUtils = new ReadFileUtils();
    @Disabled
    @Test
    public void shouldCorrectlyReplaceNameAndVerificationCode() {
        //given
        String token = "74974752-682e-47c6-98f5-d21d2b0dccad";
        String name = "Jan";
        String path = "src/test/java/pl/ziwg/backend/template/email/message_with_verification_code_template_test.html";
        //when
        String message = readFileUtils.getVerificationCodeMessage(name, token);
        //then
        assertThat(message).isEqualTo(readFileUtils.readFromFile(path));
    }
    @Disabled
    @Test
    public void shouldCorrectlyReplaceNameAndPasswordReminder() {
        //given
        String token = "0c9c4b51-0084-4b64-b637-545f6ec3712f";
        String name = "Jan";
        String path = "src/test/java/pl/ziwg/backend/template/email/message_with_password_reminder_template_test.html";
        //when
        String message = readFileUtils.getPasswordReminderMessage(name, token);
        //then
        assertThat(message).isEqualTo(readFileUtils.readFromFile(path));
    }
    @Disabled
    @Test
    public void shouldCorrectlyReplaceNameAndVaccinationDate() {
        //given
        String date = "11.12.2021";
        String name = "Jan";
        String path = "src/test/java/pl/ziwg/backend/template/email/message_with_vaccination_date_template_test.html";
        //when
        String message = readFileUtils.getVaccinationDateMessage(name, date);
        //then
        assertThat(message).isEqualTo(readFileUtils.readFromFile(path));
    }
}
