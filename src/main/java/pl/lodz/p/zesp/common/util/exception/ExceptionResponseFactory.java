package pl.lodz.p.zesp.common.util.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.lodz.p.zesp.common.util.exception.dto.BasicErrorDto;

@Log4j2
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
        log.warn(error);

        return new ResponseEntity<>(error, responseStatus);
    }
}
