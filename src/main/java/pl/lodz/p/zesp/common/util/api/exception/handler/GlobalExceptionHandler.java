package pl.lodz.p.zesp.common.util.api.exception.handler;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.zesp.common.util.api.exception.CustomHttpException;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomHttpException.class)
    public ResponseEntity<ApiExceptionResponse> handleCustomHttpException(final CustomHttpException exception) {
        log.warn("CustomHttpException", exception);
        final var response = new ApiExceptionResponse(exception);
        logResponse(response);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    private void logResponse(final ApiExceptionResponse response) {
        final var map = response.toMap();
        ThreadContext.putAll(map);
        log.warn("Response status: {}. Response body: {}", response.getStatus().value(), map);
        ThreadContext.removeAll(map.keySet());
    }
}
