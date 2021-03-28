package pl.ziwg.backend.template;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadFileUtilsTest {
    @Test
    public void shouldCorrectlyReplaceNameAndToken() {
        //given
        String token = "74974752-682e-47c6-98f5-d21d2b0dccad";
        String name = "Jan";
        String path = "src/test/java/pl/ziwg/backend/template/message_with_verification_code_template_test.txt";
        //when
        String message = ReadFileUtils.getVerificationCodeMessage(name, token);
        //then
        assertThat(message).isEqualTo(ReadFileUtils.readFromFile(path));
    }
}
