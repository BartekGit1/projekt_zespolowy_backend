package pl.lodz.p.zesp.common.util.api.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends CustomHttpException {

    public ConflictException(final String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
