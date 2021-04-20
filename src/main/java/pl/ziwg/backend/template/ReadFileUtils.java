package pl.ziwg.backend.template;

import java.io.IOException;
import java.util.Objects;

public class ReadFileUtils {
    private static final String MESSAGE_WITH_VERIFICATION_CODE_TEMPLATE_PATH =
            "/email/message_with_verification_code_template.html";

    private static final String MESSAGE_WITH_PASSWORD_REMINDER_TEMPLATE_PATH =
            "/email/message_with_password_reminder_template.html";

    private static final String MESSAGE_WITH_VACCINATION_DATE_TEMPLATE_PATH =
            "/email/message_with_vaccination_date_template.html";

    public String getVerificationCodeMessage(final String name, final String verificationCode) {
        String content = readFromFile(MESSAGE_WITH_VERIFICATION_CODE_TEMPLATE_PATH);
        String message = "";
        if (Objects.nonNull(content)) {
            message = content.replace("[name]", name).replace("[code]", verificationCode);
        }
        return message;
    }

    public String getPasswordReminderMessage(final String name, final String passwordReminder) {
        String content = readFromFile(MESSAGE_WITH_PASSWORD_REMINDER_TEMPLATE_PATH);
        String message = "";
        if (Objects.nonNull(content)) {
            message = content.replace("[name]", name).replace("[link]", passwordReminder);
        }
        return message;
    }

    public String getVaccinationDateMessage(final String name, final String vaccinationDate) {
        String content = readFromFile(MESSAGE_WITH_VACCINATION_DATE_TEMPLATE_PATH);
        String message = "";
        if (Objects.nonNull(content)) {
            message = content.replace("[name]", name).replace("[date]", vaccinationDate);
        }
        return message;
    }

    public String readFromFile(String path) {
        try {
            return new String(this.getClass().getResourceAsStream(path).readAllBytes());
        } catch (IOException e) {
            return "";
        }
    }
}
