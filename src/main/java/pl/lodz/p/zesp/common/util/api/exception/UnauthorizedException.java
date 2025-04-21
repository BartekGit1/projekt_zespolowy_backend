package pl.lodz.p.zesp.common.util.api.exception;

import org.springframework.http.HttpStatus;

public abstract class UnauthorizedException extends CustomHttpException {

    public UnauthorizedException(final String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
