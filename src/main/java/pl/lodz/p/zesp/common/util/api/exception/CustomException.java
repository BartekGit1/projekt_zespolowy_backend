package pl.lodz.p.zesp.common.util.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CustomException extends RuntimeException {
    private final String errorMessage;
}
