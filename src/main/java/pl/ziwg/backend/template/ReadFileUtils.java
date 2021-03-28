package pl.ziwg.backend.template;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ReadFileUtils {
    private static final String MESSAGE_WITH_VERIFICATION_CODE_TEMPLATE_PATH =
            "src/main/java/pl/ziwg/backend/template/message_with_verification_code_template.txt";

    public static String getVerificationCodeMessage(final String name, final String verificationCode) {
        String content = readFromFile(MESSAGE_WITH_VERIFICATION_CODE_TEMPLATE_PATH);
        String message = "";
        if (Objects.nonNull(content)) {
            message = content.replace("[name]", name).replace("[link]", verificationCode);
        }
        return message;
    }

    public static String readFromFile(String path) {
        final File file = new File(path);
        String content = null;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
