package pl.ziwg.backend.template;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadFileUtilsTest {
    @Test
    public void shouldCorrectlyReplaceNameAndVerificationCode() {
        //given
        String token = "74974752-682e-47c6-98f5-d21d2b0dccad";
        String name = "Jan";
        String path = "src/test/java/pl/ziwg/backend/template/email/message_with_verification_code_template_test.html";
        //when
        String message = ReadFileUtils.getVerificationCodeMessage(name, token);
        //then
        assertThat(message).isEqualTo(ReadFileUtils.readFromFile(path));
    }

    @Test
    public void shouldCorrectlyReplaceNameAndPasswordReminder() {
        //given
        String token = "0c9c4b51-0084-4b64-b637-545f6ec3712f";
        String name = "Jan";
        String path = "src/test/java/pl/ziwg/backend/template/email/message_with_password_reminder_template_test.html";
        //when
        String message = ReadFileUtils.getPasswordReminderMessage(name, token);
        //then
        assertThat(message).isEqualTo(ReadFileUtils.readFromFile(path));
    }

    @Test
    public void shouldCorrectlyReplaceNameAndVaccinationDate() {
        //given
        String date = "11.12.2021";
        String name = "Jan";
        String path = "src/test/java/pl/ziwg/backend/template/email/message_with_vaccination_date_template_test.html";
        //when
        String message = ReadFileUtils.getVaccinationDateMessage(name, date);
        //then
        assertThat(message).isEqualTo(ReadFileUtils.readFromFile(path));
    }
}
