package pl.lodz.p.zesp.common.util.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomHttpException extends CustomException {

    private final HttpStatus httpStatus;

    public CustomHttpException(final HttpStatus httpStatus, final String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
    }

}
