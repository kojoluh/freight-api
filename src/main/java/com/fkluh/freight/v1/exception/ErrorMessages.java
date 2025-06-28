package com.fkluh.freight.v1.exception;

public class ErrorMessages {
    public static final String PACKAGE_ALREADY_DELIVERED_CANNOT_UPDATE = "Status of the package is already set to DELIVERED. Cannot update actual delivery date.";
    public static final String TRACKING_NUMBER_EMPTY = "Tracking number cannot be null or empty";
    public static final String TRACKING_NUMBER_NOT_EXIST = "Tracking number does not exist";
    public static final String EMAIL_EMPTY = "Email cannot be null or empty";
    public static final String POSTCODE_EMPTY = "Recipient postcode cannot be null or empty";
    public static final String ACTUAL_DELIVERY_DATE_EMPTY = "Actual Delivery Date cannot be null or empty";
    public static final String PACKAGE_ALREADY_EXISTS = "Package with tracking number already exists";
    public static final String ACTUAL_DELIVERY_DATE_CANNOT_BE_FUTURE_DATE = "Actual delivery date cannot be in the future";
    public static final String DELIVERY_DATE_INVALID_FORMAT = "Delivery date must be in the format 'YYYY-MM-DD'";
    public static final String FILTER_STATUS_INVALID = "Status must be either 'delayed' or 'on-time'.";
    public static final String FILTER_INPUT_INVALID = "Invalid input data: No applicable filter strategy found.";
    public static final String TRACKING_NUMBER_OR_EMAIL_AND_POSTCODE_EMPTY = "Either tracking number or both email and postcode must be provided.";
}
