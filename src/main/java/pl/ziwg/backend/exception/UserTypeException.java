package pl.ziwg.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Given user id must belong to the citizen")
public class UserTypeException extends RuntimeException {
    public UserTypeException(String cause) {
        super(cause);
    }
}
