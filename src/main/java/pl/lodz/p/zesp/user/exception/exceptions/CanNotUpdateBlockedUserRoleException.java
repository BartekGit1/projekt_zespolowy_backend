package pl.lodz.p.zesp.user.exception.exceptions;

public class CanNotUpdateBlockedUserRoleException extends RuntimeException {
    public CanNotUpdateBlockedUserRoleException(String message) {
        super(message);
    }
}
