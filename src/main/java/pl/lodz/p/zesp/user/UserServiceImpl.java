package pl.lodz.p.zesp.user;

import jakarta.validation.Validator;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.zesp.user.dto.request.RegisterRequestDto;
import pl.lodz.p.zesp.user.dto.request.UpdateRoleDto;
import pl.lodz.p.zesp.user.dto.request.UpdateUserStatusDto;
import pl.lodz.p.zesp.user.dto.response.RegisterResponseDto;
import pl.lodz.p.zesp.user.dto.response.UserDataResponseDto;
import pl.lodz.p.zesp.user.exception.exceptions.*;
import pl.lodz.p.zesp.user.mapper.UserMapper;


import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.keycloak.events.Errors.*;
import static pl.lodz.p.zesp.user.exception.UserExceptionConstants.*;

@Service
public class UserServiceImpl implements UserService {

    private final Validator validator;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    protected static final Logger LOGGER = Logger.getGlobal();

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client.id}")
    private String clientId;

    @Value("${keycloak.realm}")
    private String realmValue;

    @Value("${keycloak.username}")
    private String keycloakUsername;

    @Value("${keycloak.password}")
    private String password;

    public UserServiceImpl(Validator validator, UserRepository userRepository, UserMapper userMapper) {
        this.validator = validator;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public RegisterResponseDto guestRegister(RegisterRequestDto registerRequestDto) {
        validator.validate(registerRequestDto);

        final UserEntity user = userMapper.toUserEntity(registerRequestDto);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            LOGGER.log(Level.INFO, "User already exists {0}, {1}", new Object[]{user, e});
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS_EXCEPTION);
        }

        return register(registerRequestDto);
    }

    private RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        final Keycloak keycloak = retrieveKeycloak();

        keycloak.tokenManager().getAccessToken();

        final UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(registerRequestDto.username());
        user.setFirstName(registerRequestDto.firstname());
        user.setLastName(registerRequestDto.lastname());
        user.setEmail(registerRequestDto.email());

        // Get realm
        final RealmResource realmResource = keycloak.realm(realm);
        final UsersResource usersRessource = realmResource.users();

        final Response response = usersRessource.create(user);

        if (response.getStatus() == 409) {
            LOGGER.log(Level.INFO, "User already exists {0}", user);
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS_EXCEPTION);
        }

        if (response.getStatus() == 201) {

            final String userId = CreatedResponseUtil.getCreatedId(response);

            // create password credential
            final CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(registerRequestDto.password());

            final UserResource userResource = usersRessource.get(userId);

            // Set password credential
            userResource.resetPassword(passwordCred);

            final ClientRepresentation client = realmResource.clients().findByClientId(clientId).get(0);
            final RoleRepresentation clientRole = realmResource.clients().get(client.getId()).roles().get(Role.CUSTOMER.toString()).toRepresentation();

            userResource.roles().clientLevel(client.getId()).add(List.of(clientRole));

        } else {
            LOGGER.log(Level.WARNING, "Keycloak error appeared");
            throw new RegistrationKeycloakError(USER_INTERNAL_EXCEPTION);
        }

        return new RegisterResponseDto(response.getStatus(), response.getStatusInfo().toString());
    }

    private Keycloak retrieveKeycloak() {
        return KeycloakBuilder.builder().serverUrl(authServerUrl)
                .grantType(OAuth2Constants.PASSWORD).realm(realmValue).clientId(clientId)
                .username(keycloakUsername).password(password)
                .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build()).build();
    }

    @Override
    public void updateUserStatus(UpdateUserStatusDto updateUserStatusDto, String username) {
        checkIfUserIsNotBlockingHimself(updateUserStatusDto.username(), username);
        checkIfUserIsNotBlockingJohndoe(updateUserStatusDto.username(), username);

        final UserEntity userEntity = userRepository.findByUsername(updateUserStatusDto.username()).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        userEntity.setAccountStatus(updateUserStatusDto.accountStatus());
        userRepository.save(userEntity);

        final Keycloak keycloak = retrieveKeycloak();
        final RealmResource realmResource = keycloak.realm(realm);
        final UsersResource usersResource = realmResource.users();
        final List<UserRepresentation> userRepresentations = usersResource
                .search(updateUserStatusDto.username())
                .stream()
                .filter(user -> user.getUsername().equals(updateUserStatusDto.username()))
                .toList();


        if (userRepresentations.isEmpty()) {
            LOGGER.log(Level.WARNING, "Specified user does not exist {0}", username);
            throw new UserNotFoundException(USER_NOTFOUND_EXCEPTION);
        }

        final UserRepresentation user = userRepresentations.get(0);

        if (updateUserStatusDto.accountStatus() == AccountStatus.ACTIVE) {
            user.setEnabled(true);

        } else if (updateUserStatusDto.accountStatus() == AccountStatus.BLOCKED) {
            user.setEnabled(false);
        }

        usersResource.get(user.getId()).update(user);
        LOGGER.log(Level.INFO, "Updated user {0} status to {1}", new Object[]{username, updateUserStatusDto.accountStatus()});
    }

    private void checkIfUserIsNotBlockingHimself(String usernameDto, String usernameJwt) {
        if (usernameDto.equals(usernameJwt)) {
            LOGGER.log(Level.WARNING, "User {0} tried to block himself", usernameJwt);
            throw new UserCanNotUpdateSelfStatusException(USER_CAN_NOT_UPDATE_SELF_STATUS_EXCEPTION);
        }
    }

    private void checkIfUserIsNotBlockingJohndoe(String usernameDto, String usernameJwt) {
        if (usernameDto.equals("johndoe")) {
            LOGGER.log(Level.WARNING, "User {0} tried to block johndoe", usernameJwt);
            throw new UserCanNotUpdateJohndoeStatusException(USER_CAN_NOT_UPDATE_JOHNDOE_STATUS_EXCEPTION);
        }
    }

    @Transactional
    @Override
    public void updateRole(UpdateRoleDto updateRoleDto, String username) {
        checkIfUserIsNotUpdatingSelfRole(updateRoleDto.username(), username);
        checkIfUserIsNotUpdatingJohndoeRole(updateRoleDto.username(), username);

        final UserEntity userEntity = userRepository.findByUsername(updateRoleDto.username()).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        checkIfUserIsNotBlocked(userEntity.getAccountStatus());
        userEntity.setRole(updateRoleDto.role());

        keycloakUpdateRole(updateRoleDto.username(), updateRoleDto.role());
        LOGGER.log(Level.INFO, "User {0} role is updated to {1}", new Object[]{updateRoleDto.username(), updateRoleDto.role()});
    }


    private void keycloakUpdateRole(String username, Role newRole) {
        final Keycloak keycloak = retrieveKeycloak();
        final RealmResource realmResource = keycloak.realm(realm);
        final UsersResource usersResource = realmResource.users();

        // Find the user by username
        final List<UserRepresentation> userRepresentations = usersResource
                .search(username)
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .toList();

        if (userRepresentations.isEmpty()) {
            LOGGER.log(Level.WARNING, "Specified employee does not exist {0}", username);
            throw new UserNotFoundException(USER_NOTFOUND_EXCEPTION);
        }

        final UserRepresentation user = userRepresentations.get(0);

        // Remove all existing roles (both realm and client roles)
        final UserResource userResource = usersResource.get(user.getId());

        // Remove realm roles
        final List<RoleRepresentation> existingRealmRoles = userResource.roles().realmLevel().listAll();
        for (RoleRepresentation role : existingRealmRoles) {
            userResource.roles().realmLevel().remove(Collections.singletonList(role));
        }

        // Remove client roles
        final List<ClientRepresentation> clients = realmResource.clients().findAll();
        for (ClientRepresentation client : clients) {
            final List<RoleRepresentation> existingClientRoles = userResource.roles().clientLevel(client.getId()).listAll();
            for (RoleRepresentation role : existingClientRoles) {
                userResource.roles().clientLevel(client.getId()).remove(Collections.singletonList(role));
            }
        }

        final ClientRepresentation client = realmResource.clients().findByClientId(clientId).get(0);

        final RoleRepresentation clientRole = realmResource.clients().get(client.getId()).roles().get(newRole.toString()).toRepresentation();

        if (Role.ADMIN == newRole) {
            final ClientRepresentation clientRepresentation = realmResource.clients().findByClientId("realm-management").get(0);

            final RoleRepresentation clientRoles = realmResource.clients().get(clientRepresentation.getId()).roles().get("view-users").toRepresentation();

            userResource.roles().clientLevel(clientRepresentation.getId()).add(List.of(clientRoles));
        }

        userResource.roles().clientLevel(client.getId()).add(List.of(clientRole));
    }


    private void checkIfUserIsNotUpdatingSelfRole(String usernameDto, String usernameJwt) {
        if (usernameDto.equals(usernameJwt)) {
            LOGGER.log(Level.WARNING, "User {0} tried to update self role", usernameJwt);
            throw new UserCanNotUpdateSelfRoleException(USER_CAN_NOT_UPDATE_SELF_ROLE_EXCEPTION);
        }
    }

    private void checkIfUserIsNotUpdatingJohndoeRole(String usernameDto, String usernameJwt) {
        if (usernameDto.equals("johndoe")) {
            LOGGER.log(Level.WARNING, "User {0} tried to update johndoe role", usernameJwt);
            throw new UserCanNotUpdateJohndoeRoleException(USER_CAN_NOT_UPDATE_JOHNDOE_ROLE_EXCEPTION);
        }
    }

    private void checkIfUserIsNotBlocked(AccountStatus accountStatus) {
        if (accountStatus == AccountStatus.BLOCKED) {
            LOGGER.log(Level.WARNING, "There was a try to update blocked user role");
            throw new CanNotUpdateBlockedUserRoleException(CAN_NOT_UPDATE_BLOCKED_USER_ROLE_EXCEPTION);
        }
    }

    @Override
    public UserDataResponseDto getUserData(String username) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        return userMapper.toDto(user);
    }
}
