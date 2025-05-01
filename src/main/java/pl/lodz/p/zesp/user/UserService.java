package pl.lodz.p.zesp.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.zesp.user.controller.UserFilter;
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

    Page<UserDataResponseDto> getUsers(UserFilter userFilter, Pageable pageable);

    void updateRolePayment(String username, Role role);
}
