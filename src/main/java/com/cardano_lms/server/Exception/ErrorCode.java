package com.cardano_lms.server.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi máy chủ không xác định. Vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Khóa không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Tên người dùng phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Tuổi của bạn phải ít nhất {min}", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(1009, "Vai trò không tồn tại", HttpStatus.NOT_FOUND),
    NONCE_NOT_EXISTED(1010, "Mã xác thực không tồn tại", HttpStatus.BAD_REQUEST),
    LOGIN_METHOD_IS_REQUIRED(1011, "Phương thức đăng nhập là bắt buộc", HttpStatus.BAD_REQUEST),
    METHOD_HAS_BEEN_EXISTED(1012, "Phương thức này đã tồn tại", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1013, "Mật khẩu là bắt buộc", HttpStatus.BAD_REQUEST),
    MISSING_CREDENTIALS(1014, "Thiếu thông tin xác thực", HttpStatus.BAD_REQUEST),
    LOGIN_METHOD_NOT_SUPPORTED(1015, "Phương thức đăng nhập không được hỗ trợ", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(1016, "Vui lòng xác minh email trước khi đăng nhập", HttpStatus.UNAUTHORIZED),
    DONT_HAVE_PERMISSION_WITH_WALLET(1016, "Bạn không có quyền với ví này", HttpStatus.BAD_REQUEST),
    WALLET_ADDRESS_REQUIRED(1017, "Địa chỉ ví là bắt buộc", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1018, "Email là bắt buộc", HttpStatus.BAD_REQUEST),
    SOCIAL_LINK_NOT_FOUND(1019, "Không tìm thấy liên kết mạng xã hội", HttpStatus.BAD_REQUEST),
    NOT_FOUND(1020, "Không tìm thấy", HttpStatus.NOT_FOUND),
    INVALID_ARGUMENT(1021, "Tham số không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_USED(1022, "Email đã được sử dụng bởi người khác", HttpStatus.BAD_REQUEST),
    WALLET_ALREADY_USED(1023, "Ví đã được sử dụng bởi người khác", HttpStatus.BAD_REQUEST),
    YOU_ARE_NOT_INSTRUCTOR(1024, "Bạn không phải là giảng viên", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_NOT_FOUND(1025, "Không tìm thấy phương thức thanh toán", HttpStatus.NOT_FOUND),
    COURSE_NOT_FOUND(1026, "Không tìm thấy khóa học", HttpStatus.NOT_FOUND),
    PROFILE_NOT_EXISTED(1027, "Không tìm thấy hồ sơ", HttpStatus.NOT_FOUND),
    CHAPTER_NOT_FOUND(1028, "Không tìm thấy chương", HttpStatus.NOT_FOUND),
    INVALID_INPUT(1029, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    ALREADY_JOIN_THIS_COURSE(1030, "Bạn đã tham gia khóa học này", HttpStatus.BAD_REQUEST),
    NOT_HAVE_METHOD(1031, "Khóa học không có phương thức thanh toán này", HttpStatus.BAD_REQUEST),
    CARDANO_TRANSACTION_NOT_VALID(1032, "Giao dịch chưa được xác minh trên Cardano blockchain!", HttpStatus.BAD_REQUEST),
    MISSING_ARGUMENT(1033, "Thiếu tham số", HttpStatus.BAD_REQUEST),
    NO_COURSE(1032, "Bạn chưa đăng ký khóa học nào", HttpStatus.NOT_FOUND),
    LECTURE_NOT_FOUND(1033, "Không tìm thấy bài giảng", HttpStatus.NOT_FOUND),
    TEST_NOT_FOUND(1034, "Không tìm thấy bài kiểm tra", HttpStatus.NOT_FOUND),
    HAVE_NOT_JOIN_THIS_COURSE(1035, "Bạn chưa tham gia khóa học này", HttpStatus.NOT_FOUND),
    ALREADY_COMPLETED(1036, "Bạn đã hoàn thành bài giảng/bài kiểm tra này", HttpStatus.BAD_REQUEST),
    DONT_CHANGE_EMAIL(1037, "Bạn đã thiết lập email và không thể thay đổi", HttpStatus.BAD_REQUEST),
    DONT_CHANGE_WALLET(1038, "Bạn đã thiết lập địa chỉ ví và không thể thay đổi", HttpStatus.BAD_REQUEST),
    THIS_COURSE_WAS_PUBLISHED(1039, "Khóa học này đã được xuất bản", HttpStatus.BAD_REQUEST),
    THIS_COURSE_IS_PRIVATE(1040, "Khóa học này ở chế độ riêng tư", HttpStatus.NOT_FOUND),
    ANSWER_NOT_FOUND(1041, "Không tìm thấy câu trả lời", HttpStatus.NOT_FOUND),
    ENROLLMENT_NOT_FOUND(1042, "Không tìm thấy đăng ký", HttpStatus.NOT_FOUND),
    COURSE_NOT_COMPLETED(1043, "Khóa học chưa hoàn thành", HttpStatus.BAD_REQUEST),
    BANED(1044, "Tài khoản của bạn đã bị khóa bởi quản trị viên", HttpStatus.NOT_FOUND),
    ALREADY_FEEDBACK(1045, "Bạn đã đánh giá khóa học này rồi", HttpStatus.BAD_REQUEST),
    FEEDBACK_NOT_FOUND(1046, "Không tìm thấy đánh giá", HttpStatus.NOT_FOUND),
    DONT_DELETE_OUR_FEEDBACK(1047, "Bạn không thể xóa đánh giá của người khác!", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(1048, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    CER_NOT_FOUND(1049, "Không tìm thấy chứng chỉ", HttpStatus.NOT_FOUND),
    DONT_CHANGE_GITHUB(1050, "Không thể thay đổi GitHub", HttpStatus.BAD_REQUEST),
    GITHUB_ALREADY_USED(1051, "GitHub này đã được sử dụng bởi người khác", HttpStatus.BAD_REQUEST),
    CANNOT_BAN_ADMIN(1052, "Không thể khóa tài khoản quản trị viên", HttpStatus.BAD_REQUEST),
    MEDIA_NOT_FOUND(1053, "Không tìm thấy media", HttpStatus.NOT_FOUND),
    TAG_NOT_FOUND(1054, "Không tìm thấy thẻ", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_AMOUNT(1055, "Số tiền thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    SLUG_HAS_BEEN_EXISTED(1055, "Thẻ này đã tồn tại slug, vui lòng đổi tên khác", HttpStatus.BAD_REQUEST),
    MUST_NOT_DELETE_COURSE(1056, "Không thể xóa khóa học đã có học viên", HttpStatus.BAD_REQUEST),
    DONT_DELETE_COURSE(1057, "Có lỗi khi xóa khóa học", HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_IN_THIS_COURSE(1058, "Đăng ký không thuộc khóa học này", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(1059, "Tải tệp lên thất bại", HttpStatus.BAD_REQUEST),
    THIS_COURSE_WAS_UNPUBLISHED(1060, "Khóa học này đang ở chế độ nháp", HttpStatus.BAD_REQUEST),
    CANNOT_UNPUBLISH_HAS_ENROLLMENTS(1061, "Không thể ẩn khóa học đã có học viên đăng ký", HttpStatus.BAD_REQUEST),
    CODE_ERROR(1062, "Mã xác nhận không chính xác", HttpStatus.BAD_REQUEST),
    ALREADY_COMPLETED_COURSERROR_CODE(1063, "Chứng chỉ đang chờ xét duyệt", HttpStatus.BAD_REQUEST),
    YOU_ARE_INSTRUCTOR(1064, "Bạn là giảng viên của khóa học này", HttpStatus.BAD_REQUEST),
    NOTIFICATION_FAILED(1065, "Có lỗi khi lấy chứng chỉ", HttpStatus.BAD_REQUEST),
    YOU_ARE_NOT_INSTRUCTOR_OF_THIS_COURSE(1066, "Bạn không phải giảng viên của khóa học này", HttpStatus.BAD_REQUEST),
    ALREADY_ENROLLED(1073, "Đã đăng ký khóa học này rồi", HttpStatus.BAD_REQUEST),
    COURSE_TITLE_EXISTED(1067, "Tiêu đề khóa học đã tồn tại", HttpStatus.BAD_REQUEST),
    DATABASE_ERROR(1068, "Lỗi lưu dữ liệu", HttpStatus.BAD_REQUEST),
    NOT_ENROLLED(1069, "Bạn chưa đăng ký khóa học này", HttpStatus.FORBIDDEN),
    PREVIOUS_LESSON_NOT_COMPLETED(1070, "Bài học trước chưa hoàn thành", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_PRICE(1071, "Giá khóa học phải là 0 (miễn phí) hoặc ít nhất 1 ADA (yêu cầu UTxO tối thiểu của Cardano)", HttpStatus.BAD_REQUEST),
    CONTRACT_SERVICE_UNAVAILABLE(1072, "Dịch vụ contract không khả dụng. Vui lòng kiểm tra CONTRACT_API_BASE_URL", HttpStatus.SERVICE_UNAVAILABLE),
    CONTRACT_MINT_FAILED(1074, "Cấp chứng chỉ thất bại. Vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),
    CONTRACT_BURN_FAILED(1075, "Thu hồi chứng chỉ thất bại. Vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),
    CERTIFICATE_ALREADY_EXISTS(1076, "Chứng chỉ đã được cấp cho khóa học này", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1077, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    BATCH_LIMIT_EXCEEDED(1078, "Tối đa 15 chứng chỉ mỗi lần cấp", HttpStatus.BAD_REQUEST),
    NO_ELIGIBLE_STUDENTS(1079, "Không có học viên đủ điều kiện để cấp chứng chỉ", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS(1080, "Quá nhiều yêu cầu. Vui lòng thử lại sau", HttpStatus.TOO_MANY_REQUESTS);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
