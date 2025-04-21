package pl.lodz.p.zesp.common.util.api.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends CustomHttpException {

    public NotFoundException(final String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
