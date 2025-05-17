package pl.lodz.p.zesp.user.dto.response;

import pl.lodz.p.zesp.user.AccountStatus;
import pl.lodz.p.zesp.user.Role;

public record ProfileDataResponseDto (
    Long id,
    String firstName,
    String lastName,
    String email,
    String username,
    Role role,
    AccountStatus status) {
}