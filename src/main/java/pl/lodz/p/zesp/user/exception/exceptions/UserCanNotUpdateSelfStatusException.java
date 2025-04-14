package pl.lodz.p.zesp.user.exception.exceptions;

public class UserCanNotUpdateSelfStatusException extends RuntimeException {
    public UserCanNotUpdateSelfStatusException(String message) {
        super(message);
    }
}
