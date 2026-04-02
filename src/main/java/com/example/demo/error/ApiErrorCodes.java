package com.example.demo.error;

// Стабильные коды для клиента (i18n)
public final class ApiErrorCodes {

  private ApiErrorCodes() {}

  public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
  public static final String CONSTRAINT_VIOLATION = "CONSTRAINT_VIOLATION";

  public static final String PASSWORD_REQUIRED = "PASSWORD_REQUIRED";
  public static final String USERNAME_TAKEN = "USERNAME_TAKEN";
  public static final String ROLE_REQUIRED = "ROLE_REQUIRED";
  public static final String ROLE_SINGLE_REQUIRED = "ROLE_SINGLE_REQUIRED";
  public static final String CANNOT_DEACTIVATE_SELF = "CANNOT_DEACTIVATE_SELF";
  public static final String CANNOT_DELETE_SELF = "CANNOT_DELETE_SELF";

  public static final String LOGIN_USER_NOT_FOUND = "LOGIN_USER_NOT_FOUND";
  public static final String LOGIN_ACCOUNT_DISABLED = "LOGIN_ACCOUNT_DISABLED";
  public static final String LOGIN_INVALID_PASSWORD = "LOGIN_INVALID_PASSWORD";

  public static final String REGISTER_USERNAME_BLANK = "REGISTER_USERNAME_BLANK";
  public static final String REGISTER_USERNAME_SIZE = "REGISTER_USERNAME_SIZE";
  public static final String REGISTER_PASSWORD_BLANK = "REGISTER_PASSWORD_BLANK";
  public static final String REGISTER_PASSWORD_SIZE = "REGISTER_PASSWORD_SIZE";

  public static final String USER_USERNAME_BLANK = "USER_USERNAME_BLANK";
  public static final String USER_USERNAME_SIZE = "USER_USERNAME_SIZE";
  public static final String USER_PASSWORD_SIZE = "USER_PASSWORD_SIZE";

  public static final String LOGIN_USERNAME_BLANK = "LOGIN_USERNAME_BLANK";
  public static final String LOGIN_PASSWORD_BLANK = "LOGIN_PASSWORD_BLANK";

  public static final String SENSOR_MODEL_REQUIRED = "SENSOR_MODEL_REQUIRED";
  public static final String SENSOR_LOCATION_REQUIRED = "SENSOR_LOCATION_REQUIRED";

  public static final String ALERT_SENSOR_ID_REQUIRED = "ALERT_SENSOR_ID_REQUIRED";
  public static final String ALERT_EVENT_TYPE_INVALID = "ALERT_EVENT_TYPE_INVALID";
  public static final String EVENT_TYPE_REQUIRED = "EVENT_TYPE_REQUIRED";

  public static final String PHOTO_URL_REQUIRED = "PHOTO_URL_REQUIRED";
  public static final String PHOTO_URL_NOT_FOR_INCIDENT = "PHOTO_URL_NOT_FOR_INCIDENT";
  public static final String PHOTO_URL_INVALID = "PHOTO_URL_INVALID";
  public static final String PHOTO_PATH_INVALID = "PHOTO_PATH_INVALID";

  public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
  public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";
  public static final String SENSOR_NOT_FOUND = "SENSOR_NOT_FOUND";
  public static final String ALERT_NOT_FOUND = "ALERT_NOT_FOUND";
  public static final String PERMISSION_NOT_FOUND = "PERMISSION_NOT_FOUND";
}
