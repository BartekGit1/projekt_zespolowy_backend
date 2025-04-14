package pl.lodz.p.zesp.user.dto.request;


import pl.lodz.p.zesp.user.Role;

public record UpdateRoleDto(
        String username,
        Role role) {
}
