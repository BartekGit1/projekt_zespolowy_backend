package pl.lodz.p.zesp.user.mapper;

import org.springframework.stereotype.Component;
import pl.lodz.p.zesp.user.AccountStatus;
import pl.lodz.p.zesp.user.Role;
import pl.lodz.p.zesp.user.UserEntity;
import pl.lodz.p.zesp.user.dto.request.RegisterRequestDto;
import pl.lodz.p.zesp.user.dto.response.UserDataResponseDto;

@Component
class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toUserEntity(RegisterRequestDto registerRequestDto) {
        return new UserEntity(
                registerRequestDto.username(),
                registerRequestDto.email(),
                AccountStatus.ACTIVE,
                Role.CUSTOMER
        );
    }

    @Override
    public UserDataResponseDto toDto(UserEntity user) {
        return new UserDataResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}
