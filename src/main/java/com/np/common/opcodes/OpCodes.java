package com.np.common.opcodes;

/**
 * This class contains all the operation codes that are sent in the beginning of any request to the server to distinguish the different types of operations.
 */
public class OpCodes {
    public static final int OP_CHECK_PRIVATE_KEY = 1;
    public static final int OP_SIGN_UP_ADMIN = 2;
    public static final int OP_SIGN_UP_CUSTOMER = 3;
    public static final int OP_LOGIN_CUSTOMER = 4;
    public static final int OP_LOGIN_ADMIN = 5;
    public static final int OP_ADD_ITEM = 6;
    public static final int OP_MODIFY_ITEM = 7;
    public static final int OP_GET_ITEMS = 8;
    public static final int OP_REMOVE_ITEM = 9;
    public static final int OP_SUBMIT_ORDER = 11;
    public static final int OP_GET_ORDERS = 12;
    public static final int OP_SUBMIT_REVIEW = 13;
}
