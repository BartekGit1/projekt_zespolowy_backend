package pl.lodz.p.zesp.user.mapper;

import pl.lodz.p.zesp.user.UserEntity;
import pl.lodz.p.zesp.user.dto.request.RegisterRequestDto;
import pl.lodz.p.zesp.user.dto.response.UserDataResponseDto;

public interface UserMapper {

    UserEntity toUserEntity(RegisterRequestDto registerRequestDto);

    UserDataResponseDto toDto(UserEntity user);
}
