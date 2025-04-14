package pl.lodz.p.zesp.user.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.zesp.common.util.exception.dto.BasicErrorDto;
import pl.lodz.p.zesp.user.exception.exceptions.*;

import static org.springframework.http.HttpStatus.*;
import static pl.lodz.p.zesp.common.util.exception.ExceptionResponseFactory.getBasicResponse;

@ControllerAdvice
class UserExceptionHandler {

    @ExceptionHandler({UserAlreadyExistsException.class})
    ResponseEntity<BasicErrorDto> userConflictException(RuntimeException ex, HttpServletRequest request) {
        return getBasicResponse(ex, request, CONFLICT);
    }

    @ExceptionHandler({RegistrationKeycloakError.class})
    ResponseEntity<BasicErrorDto> userInternalErrorException(RuntimeException ex, HttpServletRequest request) {
        return getBasicResponse(ex, request, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({UserNotFoundException.class})
    ResponseEntity<BasicErrorDto> userNotFoundException(RuntimeException ex, HttpServletRequest request) {
        return getBasicResponse(ex, request, NOT_FOUND);
    }

    @ExceptionHandler({UserCanNotUpdateSelfStatusException.class, UserCanNotUpdateJohndoeRoleException.class, CanNotUpdateBlockedUserRoleException.class, UserCanNotUpdateSelfRoleException.class})
    ResponseEntity<BasicErrorDto> userForbiddenException(RuntimeException ex, HttpServletRequest request) {
        return getBasicResponse(ex, request, FORBIDDEN);
    }

}