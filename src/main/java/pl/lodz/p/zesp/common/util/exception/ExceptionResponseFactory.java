package pl.lodz.p.zesp.common.util.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.lodz.p.zesp.common.util.exception.dto.BasicErrorDto;

public class ExceptionResponseFactory {

    private ExceptionResponseFactory() {
    }

    public static ResponseEntity<BasicErrorDto> getBasicResponse(
            Exception e,
            HttpServletRequest request,
            HttpStatus responseStatus
    ) {
        BasicErrorDto error = new BasicErrorDto(
                responseStatus.value(),
                responseStatus.getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, responseStatus);
    }
}
