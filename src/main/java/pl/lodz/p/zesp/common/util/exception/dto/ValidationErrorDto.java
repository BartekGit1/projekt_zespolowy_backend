package pl.lodz.p.zesp.common.util.exception.dto;

import java.util.Map;

public record ValidationErrorDto(
        int statusCode,
        String statusText,
        Map<String, String> errors,
        String path
) {
}
