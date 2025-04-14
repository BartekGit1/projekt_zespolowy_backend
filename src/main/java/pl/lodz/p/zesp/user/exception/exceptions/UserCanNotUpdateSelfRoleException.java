package pl.lodz.p.zesp.user.exception.exceptions;

public class UserCanNotUpdateSelfRoleException extends RuntimeException {
    public UserCanNotUpdateSelfRoleException(String message) {
        super(message);
    }
}
