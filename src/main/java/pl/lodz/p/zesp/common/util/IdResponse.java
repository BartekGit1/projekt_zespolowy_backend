package pl.lodz.p.zesp.common.util;

import jakarta.validation.constraints.NotNull;

public record IdResponse(
        @NotNull
        Long id
) {
}
