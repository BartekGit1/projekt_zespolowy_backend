package pl.lodz.p.zesp.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static pl.lodz.p.zesp.user.exception.UserExceptionConstants.*;

public record RegisterRequestDto(
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,10}$",
                message = INVALID_EMAIL_PATTERN)
        @NotBlank(message = INVALID_EMAIL_PATTERN)
        String email,
        @NotBlank(message = INVALID_USERNAME_PATTERN)
        @Size(min = 6, max = 32, message = INVALID_USERNAME_PATTERN)
        String username,
        @NotBlank(message = INVALID_FIRSTNAME_PATTERN)
        @Size(min = 2, max = 32, message = INVALID_FIRSTNAME_PATTERN)
        String firstname,
        @Size(min = 2, max = 32, message = INVALID_LASTNAME_PATTERN)
        @NotBlank(message = INVALID_LASTNAME_PATTERN)
        String lastname,
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$",
                message = INVALID_PASSWORD_PATTERN)
        @NotBlank(message = INVALID_PASSWORD_PATTERN)
        String password,
        String language,
        String biography) {
}