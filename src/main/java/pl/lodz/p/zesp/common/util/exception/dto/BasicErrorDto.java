package pl.lodz.p.zesp.common.util.exception.dto;

public record BasicErrorDto(int statusCode, String statusText, String message, String path) {
}
