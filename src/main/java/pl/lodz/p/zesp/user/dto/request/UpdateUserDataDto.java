package pl.lodz.p.zesp.user.dto.request;

public record UpdateUserDataDto(
        String firstname,
        String lastname,
        String email
) {
}
