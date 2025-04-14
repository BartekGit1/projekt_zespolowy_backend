package pl.lodz.p.zesp.user.dto.response;

import pl.lodz.p.zesp.user.Role;

public record UserDataResponseDto(Long id, String username, Role role) {
}