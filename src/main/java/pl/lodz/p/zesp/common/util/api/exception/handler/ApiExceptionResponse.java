package pl.lodz.p.zesp.common.util.api.exception.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import pl.lodz.p.zesp.common.util.api.exception.CustomHttpException;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ApiExceptionResponse {
    private final HttpStatus status;
    private final String message;
    private final String className;

    public ApiExceptionResponse(final CustomHttpException exception) {
        this.status = exception.getHttpStatus();
        this.message = exception.getErrorMessage();
        this.className = exception.getClass().getSimpleName();
    }

    public Map<String, String> toMap() {
        final var map = new HashMap<String, String>();
        map.put("status", status.toString());
        map.put("message", message);
        map.put("className", className);
        return map;
    }
}
