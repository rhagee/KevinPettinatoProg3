package utils;

//Response coming from Server to Client to trigger UI changes/Callbacks
public enum ResponseCodes {
    OK,
    DISCONNECTED,
    UPDATE,
    UNAUTHORIZED,
    FORBIDDEN,
    NOTFOUND,
    UNHANDLED,
    BADPARAM
}
