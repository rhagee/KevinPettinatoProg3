package utils;

//Switch case in Socket Thread will use this to decide what flow to trigger ServerSide
public enum RequestCodes {
    AUTH,
    SEND,
    RECEIVE,
    DELETE
}
