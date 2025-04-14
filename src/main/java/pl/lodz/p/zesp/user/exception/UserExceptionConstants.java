package pl.lodz.p.zesp.user.exception;

public class UserExceptionConstants {
    public static final String INVALID_EMAIL_PATTERN = "exception.registration.receiver_email";
    public static final String INVALID_USERNAME_PATTERN = "exception.registration.username";
    public static final String INVALID_FIRSTNAME_PATTERN = "exception.registration.firstname";
    public static final String INVALID_LASTNAME_PATTERN = "exception.registration.lastname";
    public static final String INVALID_PASSWORD_PATTERN = "exception.registration.password";
    public static final String USER_ALREADY_EXISTS_EXCEPTION = "exception.registration.user_already_exists";
    public static final String USER_INTERNAL_EXCEPTION = "exception.user.internal_error";
    public static final String USER_NOTFOUND_EXCEPTION = "exception.user.notfound";
    public static final String USER_EMPTY_STATUS_EXCEPTION = "exception.user.empty_account_status";
    public static final String USER_CAN_NOT_UPDATE_JOHNDOE_STATUS_EXCEPTION = "exception.user.update_johndoe_status";
    public static final String USER_CAN_NOT_UPDATE_SELF_STATUS_EXCEPTION = "exception.user.update_self_status";
    public static final String USER_CAN_NOT_UPDATE_SELF_ROLE_EXCEPTION = "exception.user.update_self_role";
    public static final String USER_CAN_NOT_UPDATE_JOHNDOE_ROLE_EXCEPTION = "exception.user.update_johndoe_role";
    public static final String CAN_NOT_UPDATE_BLOCKED_USER_ROLE_EXCEPTION = "exception.user.update_blocked_user_role";

    private UserExceptionConstants() {
    }
}
