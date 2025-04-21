package pl.lodz.p.zesp.common.util.api.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends CustomHttpException {

    public ForbiddenException(final String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
