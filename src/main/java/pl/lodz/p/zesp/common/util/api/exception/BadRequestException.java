package pl.lodz.p.zesp.common.util.api.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends CustomHttpException {

    public BadRequestException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
