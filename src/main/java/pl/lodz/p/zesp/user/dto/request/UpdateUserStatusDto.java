package pl.lodz.p.zesp.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.lodz.p.zesp.user.AccountStatus;

import static pl.lodz.p.zesp.user.exception.UserExceptionConstants.INVALID_USERNAME_PATTERN;
import static pl.lodz.p.zesp.user.exception.UserExceptionConstants.USER_EMPTY_STATUS_EXCEPTION;

public record UpdateUserStatusDto(
        @Size(min = 6, max = 32, message = INVALID_USERNAME_PATTERN)
        String username,

        @NotNull(message = USER_EMPTY_STATUS_EXCEPTION)
        AccountStatus accountStatus) {
}
