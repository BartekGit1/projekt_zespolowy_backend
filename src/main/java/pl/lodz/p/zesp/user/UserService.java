package pl.lodz.p.zesp.user;

import pl.lodz.p.zesp.user.dto.request.RegisterRequestDto;
import pl.lodz.p.zesp.user.dto.request.UpdateRoleDto;
import pl.lodz.p.zesp.user.dto.request.UpdateUserStatusDto;
import pl.lodz.p.zesp.user.dto.response.RegisterResponseDto;
import pl.lodz.p.zesp.user.dto.response.UserDataResponseDto;

public interface UserService {

    RegisterResponseDto guestRegister(RegisterRequestDto registerRequestDto);

    void updateUserStatus(UpdateUserStatusDto updateUserStatusDto, String username);

    void updateRole(UpdateRoleDto updateRoleDto, String username);

    UserDataResponseDto getUserData(String username);
}
