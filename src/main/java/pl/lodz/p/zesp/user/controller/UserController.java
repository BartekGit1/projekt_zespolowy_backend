package pl.lodz.p.zesp.user.controller;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.zesp.user.UserService;
import pl.lodz.p.zesp.user.dto.request.RegisterRequestDto;
import pl.lodz.p.zesp.user.dto.request.UpdateRoleDto;
import pl.lodz.p.zesp.user.dto.request.UpdateUserDataDto;
import pl.lodz.p.zesp.user.dto.request.UpdateUserStatusDto;
import pl.lodz.p.zesp.user.dto.response.ProfileDataResponseDto;
import pl.lodz.p.zesp.user.dto.response.RegisterResponseDto;
import pl.lodz.p.zesp.user.dto.response.UserDataResponseDto;

import static pl.lodz.p.zesp.common.util.Constants.*;

@RestController
@RequestMapping(URI_VERSION_V1 + URI_USERS)
class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(URI_REGISTER)
    public ResponseEntity<RegisterResponseDto> guestRegister(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        final RegisterResponseDto register = userService.guestRegister(registerRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(register);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/update-status")
    public ResponseEntity<Void> updateStatus(@Valid @RequestBody UpdateUserStatusDto updateUserStatusDto, Authentication authentication) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");

        userService.updateUserStatus(updateUserStatusDto, username);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/update-role")
    public ResponseEntity<Void> updateRole(@Valid @RequestBody UpdateRoleDto updateRoleDto, Authentication authentication) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");

        userService.updateRole(updateRoleDto, username);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUser/{username}")
    ResponseEntity<UserDataResponseDto> getUserData(@PathVariable String username) {
        final UserDataResponseDto userDataResponseDto = userService.getUserData(username);

        return ResponseEntity.status(HttpStatus.OK).body(userDataResponseDto);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN','PREMIUM')")
    @GetMapping("/getSelfData")
    ResponseEntity<UserDataResponseDto> getSelfData(Authentication authentication) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");
        final UserDataResponseDto userDataResponseDto = userService.getUserData(username);

        return ResponseEntity.status(HttpStatus.OK).body(userDataResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    ResponseEntity<Page<UserDataResponseDto>> getUsers(
            final UserFilter userFilter,
            final Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getUsers(userFilter, pageable));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'PREMIUM')")
    @GetMapping("/profileData/{username}")
    public ResponseEntity<ProfileDataResponseDto> getProfileData(@PathVariable String username) {
         return ResponseEntity.ok( userService.getProfileData(username));
    }



    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'PREMIUM')")
    @PatchMapping("/update-data")
    public ResponseEntity<Void> updateUserData(@Valid @RequestBody UpdateUserDataDto updateUserDataDto, Authentication authentication) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");

        userService.updateUserData(updateUserDataDto, username);

        return ResponseEntity.noContent().build();
    }





}
