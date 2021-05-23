package pl.ziwg.backend.template;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

public class ReadFileUtils {
    private static final String MESSAGE_WITH_VERIFICATION_CODE_TEMPLATE_PATH =
            "/email/message_with_verification_code_template.html";

    private static final String MESSAGE_WITH_PASSWORD_REMINDER_TEMPLATE_PATH =
            "/email/message_with_password_reminder_template.html";

    private static final String MESSAGE_WITH_VACCINATION_DATE_TEMPLATE_PATH =
            "/email/message_with_vaccination_date_template.html";

    private static final String MESSAGE_WITH_VISIT_COMFIRMATION_TEMPLATE_PATH =
            "/email/message_with_visit_confirmation_template.html";

    private static final String MESSAGE_WITH_APPOINTMENT_REMINDER_TEMPLATE_PATH =
            "/email/message_with_appointment_reminder_template.html";

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

    public String getVisitConfirmationMessage(final String name, final String vaccinationDate) {
        String content = readFromFile(MESSAGE_WITH_VISIT_COMFIRMATION_TEMPLATE_PATH);
        String message = "";
        if (Objects.nonNull(content)) {
            message = content.replace("[name]", name).replace("[date]", vaccinationDate);
        }
        return message;
    }

    public String getAppointmentReminderMessage(final LocalDateTime vaccinationDate) {
        String content = readFromFile(MESSAGE_WITH_APPOINTMENT_REMINDER_TEMPLATE_PATH);
        String message = "";
        if (Objects.nonNull(content)) {
            message = content.replace("[date]", parseDate(vaccinationDate));
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

    public static String parseDate(LocalDateTime time) {
        return String.format("%s.%s.%d %s:%s",
                Integer.toString(time.getDayOfMonth()).length() == 1 ?
                        "0".concat(Integer.toString(time.getDayOfMonth())) : Integer.toString(time.getDayOfMonth()) ,
                Integer.toString(time.getMonthValue()).length() == 1 ?
                        "0".concat(Integer.toString(time.getMonthValue())) : Integer.toString(time.getMonthValue()),
                time.getYear(),
                Integer.toString(time.getHour()).length() == 1 ?
                        "0".concat(Integer.toString(time.getHour())) : Integer.toString(time.getHour()),
                Integer.toString(time.getMinute()).length() == 1 ?
                        "0".concat(Integer.toString(time.getMinute())) : Integer.toString(time.getMinute()));
    }
}
