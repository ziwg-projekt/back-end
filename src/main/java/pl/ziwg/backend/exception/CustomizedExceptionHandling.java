package pl.ziwg.backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.hibernate.id.IdentifierGenerationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomizedExceptionHandling extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TokenDoesNotExistsException.class)
    public ResponseEntity<ApiError> handleTokenDoesNotExistsException(TokenDoesNotExistsException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectRegistrationCodeException.class)
    public ResponseEntity<ApiError> handleIncorrectRegistrationCodeException(IncorrectRegistrationCodeException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RegistrationCodeExpiredException.class)
    public ResponseEntity<ApiError> handleRegistrationCodeExpiredException(RegistrationCodeExpiredException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PeselDoesNotExistsException.class)
    public ResponseEntity<ApiError> handlePeselDoesNotExistsException(PeselDoesNotExistsException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectRequestParametersException.class)
    public ResponseEntity<ApiError> handleInvalidRequestException(IncorrectRequestParametersException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDataTypeInPayloadException.class)
    public ResponseEntity<ApiError> handleInvalidDataTypeInPayloadException(InvalidDataTypeInPayloadException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(NotSupportedCommunicationChannelException.class)
    public ResponseEntity<ApiError> handleNotSupportedCommunicationChannelException(NotSupportedCommunicationChannelException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleUserAlreadyRegisteredException(UserAlreadyRegisteredException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationAlreadySucceededException.class)
    public ResponseEntity<ApiError> handleVerificationAlreadySucceededException(VerificationAlreadySucceededException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.GONE);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request){
        return new ResponseEntity<>(new ApiError(ex), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(new ApiError("Give JSON with appropriate values in request body!", ex.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(new ApiError(ex.getMessage(), ex.getClass().getSimpleName()), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleRuntimeException(AuthenticationException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNoSuchResourceException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IdentifierGenerationException.class)
    public ResponseEntity<ApiError> handleIdentifierGenerationException(IdentifierGenerationException exception) {
        return new ResponseEntity<>(new ApiError("Probably wrong PK column name!", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiError> handleInvalidFormatException(InvalidFormatException exception) {
        return new ResponseEntity<>(new ApiError(exception.getCause().toString(), exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiError> handleMultipartException(MultipartException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotAvailableException.class)
        public ResponseEntity<ApiError> handleUsernameNotAvailableException(UsernameNotAvailableException exception) {
            return new ResponseEntity<>(new ApiError(exception.getMessage(), exception.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }
}
