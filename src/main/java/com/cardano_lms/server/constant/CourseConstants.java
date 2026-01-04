package com.cardano_lms.server.constant;

public final class CourseConstants {

    private CourseConstants() {}

    public static final String FREE_ENROLL_METHOD = "FREE_ENROLL";
    public static final String FREE_METHOD_ALT = "FREE";
    public static final String DEFAULT_RECEIVER_ADDRESS = "";
    
    public static final int MIN_PAID_PRICE_ADA = 1;

    public static final String ACTIVITY_COURSE_CREATED = "course_created";
    public static final String ACTIVITY_COURSE_UPDATED = "course_updated";
    public static final String ACTIVITY_COURSE_PUBLISHED = "course_published";
    public static final String ACTIVITY_COURSE_UNPUBLISHED = "course_unpublished";

    public static final String DESC_COURSE_CREATED = "Tạo khóa học: ";
    public static final String DESC_COURSE_UPDATED = "Cập nhật khóa học: ";
    public static final String DESC_COURSE_PUBLISHED = "Xuất bản khóa học: ";
    public static final String DESC_COURSE_UNPUBLISHED = "Ẩn khóa học: ";

    public static final String SORT_POPULAR = "popular";
    public static final String SORT_NEWEST = "newest";
    public static final String SORT_PRICE = "price";

    public static final String DEFAULT_SORT_FIELD = "createdAt";
}
