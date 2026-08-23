package fr.trollgun.optimiam.common.exception;

public enum ErrorCode {
    // 400 Bad Request
    INVALID_QUANTITY,
    INVALID_OPERATION,
    VALIDATION_FAILED,

    // 404 Not Found
    PRODUCT_NOT_FOUND,
    CATEGORY_NOT_FOUND,
    STOCK_NOT_FOUND,
    RECIPE_NOT_FOUND,
    MEAL_PLAN_NOT_FOUND,
    SHOPPING_LIST_NOT_FOUND,
    USER_NOT_FOUND,

    // 409 Conflict
    CONFLICT,
    INSUFFICIENT_STOCK,
    SYNC_CONFLICT,

    // 500 Internal Server Error
    HARDWARE_ERROR,
    INTERNAL_SERVER_ERROR
}
