package com.np.common.responsecodes;

/**
 * This class represents all the response codes that are received by the client for each request type, they are sent at the beginning of the response.
 */
public class ResponseCodes {

    public static final int RESPONSE_USERNAME_ALREADY_EXIST = 1;
    public static final int RESPONSE_PRIVATE_CODE_IS_WRONG = 2;
    public static final int RESPONSE_SIGNUP_SUCCEEDED = 3;
    public static final int RESPONSE_USERNAME_WRONG = 4;
    public static final int RESPONSE_PASSWORD_WRONG = 5;
    public static final int RESPONSE_LOGIN_SUCCEEDED = 6;
    public static final int RESPONSE_ITEM_ADDED_SUCCESSFULLY = 7;
    public static final int RESPONSE_FAILED_TO_ADD_NEW_ITEM = 8;
    public static final int RESPONSE_ITEM_MODIFIED_SUCCESSFULLY = 9;
    public static final int RESPONSE_FAILED_TO_MODIFY_ITEM = 10;
    public static final int RESPONSE_GET_ITEMS_SUCCESSFULLY = 11;
    public static final int RESPONSE_FAILED_GET_ITEMS = 12;
    public static final int RESPONSE_REMOVE_ITEM_FAILED = 13;
    public static final int RESPONSE_REMOVE_ITEM_SUCCEED = 14;
    public static final int RESPONSE_SUBMIT_ORDER_SUCCEED = 15;
    public static final int RESPONSE_SUBMIT_ORDER_FAILED = 16;
    public static final int RESPONSE_GET_ORDERS_SUCCEED = 17;
    public static final int RESPONSE_GET_ORDERS_FAILED = 18;
    public static final int RESPONSE_REVIEW_SUBMITTED = 19;
    public static final int RESPONSE_REVIEW_FAILED = 20;
}
