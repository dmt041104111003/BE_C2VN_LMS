package com.cardano_lms.server.Controller;

import com.cardano_lms.server.constant.CourseType;
import com.cardano_lms.server.DTO.Request.*;
import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.DTO.Request.UpdateDiscountRequest;
import com.cardano_lms.server.DTO.Request.UpdateTestPassScoreRequest;
import com.cardano_lms.server.Service.CourseService;
import com.cardano_lms.server.Service.CourseActivityService;
import com.cardano_lms.server.Service.EnrollmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.List;

@Tag(name = "Khóa học", description = "API quản lý khóa học: tạo, tìm kiếm, chi tiết, chương/bài giảng, bài kiểm tra, xuất bản")
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseController {

        CourseService courseService;
        EnrollmentService enrollmentService;
        CourseActivityService courseActivityService;
        ObjectMapper objectMapper;

        @Operation(summary = "Dashboard của giảng viên", description = "Lấy dữ liệu tổng quan khóa học của giảng viên")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn thành công")
        })
        @GetMapping("/educator/dashboard/{educatorId}")
        public ApiResponse<List<CourseDashboardResponse>> getCourseEducatorDashboardData(
                        @Parameter(description = "ID giảng viên", required = true) @PathVariable String educatorId) {
                return ApiResponse.<List<CourseDashboardResponse>>builder()
                                .result(courseService.getEducatorDashboard(educatorId))
                                .build();
        }

        @Operation(summary = "Tạo khóa học", description = "Tạo mới khóa học với dữ liệu và ảnh đại diện")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo khóa học thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
        })
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ApiResponse<CourseCreationResponse> createCourse(
                        @Parameter(description = "JSON dữ liệu khóa học", required = true) @RequestPart("data") String courseDataJson,
                        @Parameter(description = "Ảnh đại diện khóa học") @RequestPart(value = "image", required = false) MultipartFile imageFile)
                        throws IOException {
                CourseCreationRequest courseCreationRequest = objectMapper.readValue(courseDataJson,
                        CourseCreationRequest.class);
                courseCreationRequest.setImage(imageFile);
                return ApiResponse.<CourseCreationResponse>builder()
                                .result(courseService.createCourse(courseCreationRequest))
                                .build();
        }

        @Operation(summary = "Lấy tất cả khóa học", description = "Trả về danh sách rút gọn tất cả khóa học")
        @GetMapping
        public ApiResponse<List<CourseSummaryResponse>> getAll() {
                return ApiResponse.<List<CourseSummaryResponse>>builder()
                                .result(courseService.getCourses())
                                .build();
        }

        @Operation(summary = "Tìm kiếm khóa học", description = "Tìm kiếm và phân trang theo từ khóa, loại khóa học, giá, thẻ và sắp xếp")
        @GetMapping("/search")
        public ApiResponse<PageResponse<CourseSummaryResponse>> searchCourses(
                        @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
                        @Parameter(description = "Loại khóa học") @RequestParam(required = false) CourseType courseType,
                        @Parameter(description = "Giá tối thiểu") @RequestParam(required = false) Integer minPrice,
                        @Parameter(description = "Giá tối đa") @RequestParam(required = false) Integer maxPrice,
                        @Parameter(description = "ID thẻ") @RequestParam(required = false) String tagId,
                        @Parameter(description = "Tiêu chí sắp xếp") @RequestParam(required = false) String sort,
                        @Parameter(description = "Trang hiện tại (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
                PageResponse<CourseSummaryResponse> result = courseService.searchCourses(keyword, courseType, minPrice,
                                maxPrice, tagId, sort, page, size);

                return ApiResponse.<PageResponse<CourseSummaryResponse>>builder()
                                .result(result)
                                .build();
        }

        @Operation(summary = "Lấy khóa học theo thẻ", description = "Lọc khóa học theo thẻ và phân trang")
        @GetMapping("/by-tag")
        public ApiResponse<PageResponse<CourseSummaryResponse>> getCoursesByTag(
                        @Parameter(description = "ID thẻ", required = true) @RequestParam String tagId,
                        @Parameter(description = "Trang hiện tại (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
                PageResponse<CourseSummaryResponse> result = courseService.searchCourses(null, null, null, null, tagId,
                                null, page, size);

                return ApiResponse.<PageResponse<CourseSummaryResponse>>builder()
                                .result(result)
                                .build();
        }

        @Operation(summary = "Lấy chi tiết khóa học theo slug", description = "Trả về thông tin chi tiết khóa học theo slug URL")
        @GetMapping("/slug/{slug}")
        public ApiResponse<CourseDetailResponse> getCourseBySlug(
                        @Parameter(description = "Slug khóa học", required = true) @PathVariable String slug,
                        @Parameter(description = "ID người dùng (tùy chọn)") @RequestParam(required = false) String userId,
                        @Parameter(description = "ID giảng viên (tùy chọn)") @RequestParam(required = false) String instructorId) {
                return ApiResponse.<CourseDetailResponse>builder()
                                .result(courseService.getCourseBySlug(slug, userId, instructorId))
                                .build();
        }

        @Operation(summary = "Lấy chi tiết khóa học theo ID", description = "Trả về thông tin chi tiết khóa học cho trang khám phá/chi tiết")
        @GetMapping("/{courseId}")
        public ApiResponse<CourseDetailResponse> getCourseById(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "ID người dùng (tùy chọn)") @RequestParam(required = false) String userId,
                        @Parameter(description = "ID giảng viên (tùy chọn)") @RequestParam(required = false) String instructorId) {
                return ApiResponse.<CourseDetailResponse>builder()
                                .result(courseService.getCourseById(courseId, userId, instructorId))
                                .build();
        }

        @Operation(summary = "Lấy nội dung học tập", description = "Trả về nội dung khóa học cho học viên đã đăng ký (yêu cầu đăng nhập và đã enrollment)")
        @GetMapping("/learn/{slug}")
        public ApiResponse<CourseDetailResponse> getLearningContent(
                        @Parameter(description = "Slug khóa học", required = true) @PathVariable String slug) {
                return ApiResponse.<CourseDetailResponse>builder()
                                .result(courseService.getLearningContent(slug))
                                .build();
        }

        @Operation(summary = "Cập nhật khóa học", description = "Cập nhật thông tin khóa học với dữ liệu JSON và ảnh mới (tùy chọn)")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học")
        })
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ApiResponse<CourseUpdateResponse> updateCourse(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String id,
                        @Parameter(description = "JSON dữ liệu khóa học", required = true) @RequestPart("data") String courseDataJson,
                        @Parameter(description = "Ảnh đại diện mới (tùy chọn)") @RequestPart(value = "image", required = false) MultipartFile image)
                        throws IOException {
                CourseUpdateRequest request = objectMapper.readValue(courseDataJson, CourseUpdateRequest.class);
                request.setImage(image);
                return ApiResponse.<CourseUpdateResponse>builder()
                                .result(courseService.updateCourse(id, request))
                                .message("Cập nhật khóa học thành công")
                                .build();
        }

        @Operation(summary = "Xóa khóa học", description = "Xóa một khóa học theo ID")
        @DeleteMapping("/{id}")
        public ApiResponse<Void> deleteCourse(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String id)
                        throws IOException {
                courseService.deleteCourse(id);
                return ApiResponse.<Void>builder()
                                .message("Course deleted successfully")
                                .build();
        }

        @Operation(summary = "Thêm chương vào khóa học", description = "Tạo mới chương cho một khóa học")
        @PostMapping("/{courseId}/chapters")
        public ApiResponse<ChapterResponse> addChapter(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "Dữ liệu chương", required = true) @RequestBody ChapterRequest request) {

                ChapterResponse chapterResponse = courseService.addChapterToCourse(courseId, request);
                return ApiResponse.<ChapterResponse>builder()
                                .message("Chapter created successfully")
                                .result(chapterResponse)
                                .build();
        }

        @Operation(summary = "Thêm bài giảng vào chương", description = "Tạo bài giảng mới trong một chương")
        @PostMapping("/chapterId={chapterId}/lectures")
        public ApiResponse<LectureResponse> addLectureToChapter(
                        @Parameter(description = "ID chương", required = true) @PathVariable Long chapterId,
                        @Parameter(description = "Dữ liệu bài giảng", required = true) @RequestBody LectureRequest request) {

                LectureResponse lectureResponse = courseService.addLectureToChapter(chapterId, request);
                return ApiResponse.<LectureResponse>builder()
                                .message("Lecture created successfully")
                                .result(lectureResponse)
                                .build();
        }

        @Operation(summary = "Thêm bài kiểm tra vào chương", description = "Tạo bài kiểm tra trong một chương")
        @PostMapping("/chapterId={chapterId}/tests")
        public ApiResponse<TestResponse> addLectureToChapter(
                        @Parameter(description = "ID chương", required = true) @PathVariable Long chapterId,
                        @Parameter(description = "Dữ liệu bài kiểm tra", required = true) @RequestBody TestRequest request) {

                TestResponse testResponse = courseService.addTest(request, chapterId, null);
                return ApiResponse.<TestResponse>builder()
                                .message("Lecture created successfully")
                                .result(testResponse)
                                .build();
        }

        @Operation(summary = "Thêm bài kiểm tra vào khóa học", description = "Tạo bài kiểm tra trực tiếp cho khóa học")
        @PostMapping("/courseId={courseId}/tests")
        public ApiResponse<TestResponse> addLectureToCourse(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "Dữ liệu bài kiểm tra", required = true) @RequestBody TestRequest request) {

                TestResponse testResponse = courseService.addTest(request, null, courseId);
                return ApiResponse.<TestResponse>builder()
                                .message("Test created successfully")
                                .result(testResponse)
                                .build();
        }

        @Operation(summary = "Lấy khóa học theo hồ sơ", description = "Phân trang danh sách khóa học theo profile ID")
        @GetMapping("/profile/{profileId}")
        public ApiResponse<PageResponse<CourseSummaryResponse>> getCoursesByProfile(
                        @Parameter(description = "ID hồ sơ giảng viên", required = true) @PathVariable String profileId,
                        @Parameter(description = "Trang hiện tại (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
                PageResponse<CourseSummaryResponse> courses = courseService.getCoursesByProfile(profileId, page, size);
                return ApiResponse.<PageResponse<CourseSummaryResponse>>builder()
                                .result(courses)
                                .message("Course list retrieved successfully")
                                .build();
        }

        @Operation(summary = "Lấy bản nháp theo hồ sơ", description = "Phân trang danh sách khóa học nháp theo profile ID")
        @GetMapping("/profile/{profileId}/drafts")
        public ApiResponse<PageResponse<CourseSummaryResponse>> getDraftCoursesByProfile(
                        @Parameter(description = "ID hồ sơ giảng viên", required = true) @PathVariable String profileId,
                        @Parameter(description = "Trang hiện tại (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
                PageResponse<CourseSummaryResponse> courses = courseService.getCoursesByProfileDrafts(profileId, page,
                                size);
                return ApiResponse.<PageResponse<CourseSummaryResponse>>builder()
                                .result(courses)
                                .message("Draft course list retrieved successfully")
                                .build();
        }

        @Operation(summary = "Lấy tất cả khóa học của tôi (giảng viên)", description = "Phân trang danh sách khóa học của giảng viên hiện tại")
        @GetMapping("/profile/me/all")
        public ApiResponse<PageResponse<CourseShortInformationResponse>> getMyCoursesAll(
                        @Parameter(description = "Trang hiện tại (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
                PageResponse<CourseShortInformationResponse> courses = courseService.getMyCoursesAll(page, size);
                return ApiResponse.<PageResponse<CourseShortInformationResponse>>builder()
                                .result(courses)
                                .message("All courses of current instructor retrieved successfully")
                                .build();
        }

        @Operation(summary = "Danh sách khóa học công khai của tôi (giảng viên)", description = "Lấy danh sách rút gọn các khóa học công khai của giảng viên hiện tại")
        @GetMapping("/profile/me/public")
        public ApiResponse<List<CourseShortInformationResponse>> getMyShortCourses() {
                return ApiResponse.<List<CourseShortInformationResponse>>builder()
                                .result(courseService.getMyShortCourses())
                                .build();
        }

        @Operation(summary = "Thống kê đăng ký theo khóa học", description = "Trả về số liệu/chi tiết đăng ký của một khóa học")
        @GetMapping("/{courseId}/enrolled")
        public ApiResponse<CourseEnrolledResponse> getEnrolled(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                return ApiResponse.<CourseEnrolledResponse>builder()
                                .code(1000)
                                .message("Enrolled query successfully")
                                .result(enrollmentService.getAllEnrolledByCourse(courseId))
                                .build();
        }

        @Operation(summary = "Lấy bài kiểm tra của khóa học", description = "Lấy thông tin một bài kiểm tra theo ID trong khóa học")
        @GetMapping("/{courseId}/tests/{testId}")
        public ApiResponse<TestResponse> getTest(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "ID bài kiểm tra", required = true) @PathVariable Long testId) {
                return ApiResponse.<TestResponse>builder()
                                .result(courseService.getTest(courseId, testId))
                                .message("Course list retrieved successfully")
                                .build();
        }

        @Operation(summary = "Nộp bài kiểm tra", description = "Nộp bài làm và chấm điểm bài kiểm tra")
        @PostMapping("/{courseId}/tests/submit/{testId}")
        public ApiResponse<TestResultResponse> submitTest(
                        @Parameter(description = "Dữ liệu bài nộp", required = true) @RequestBody TestSubmissionRequest submission,
                        @Parameter(description = "ID bài kiểm tra", required = true) @PathVariable Long testId,
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                TestResultResponse result = courseService.evaluateTest(submission, testId, courseId);
                return ApiResponse.<TestResultResponse>builder()
                                .result(result)
                                .message("Test submitted successfully")
                                .build();
        }

        @Operation(summary = "Lấy kết quả bài test đã pass", description = "Lấy kết quả bài test đã pass trước đó của user")
        @GetMapping("/{courseId}/tests/{testId}/result")
        public ApiResponse<TestResultResponse> getPreviousTestResult(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "ID bài kiểm tra", required = true) @PathVariable Long testId,
                        @Parameter(description = "ID người dùng", required = true) @RequestParam String userId) {
                TestResultResponse result = courseService.getPreviousTestResult(userId, testId, courseId);
                return ApiResponse.<TestResultResponse>builder()
                                .result(result)
                                .build();
        }

        @Operation(summary = "Xuất bản khóa học", description = "Chuyển trạng thái khóa học sang công khai")
        @PutMapping("/publish/{courseId}")
        public ApiResponse<CourseSummaryResponse> publishCourse(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                CourseSummaryResponse result = courseService.publishCourse(courseId);
                return ApiResponse.<CourseSummaryResponse>builder()
                                .result(result)
                                .message("Course change status publish successfully")
                                .build();
        }

        @Operation(summary = "Gỡ xuất bản khóa học", description = "Chuyển trạng thái khóa học sang riêng tư")
        @PutMapping("/unpublish/{courseId}")
        public ApiResponse<CourseSummaryResponse> unPublishCourse(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                CourseSummaryResponse result = courseService.unPublishCourse(courseId);
                return ApiResponse.<CourseSummaryResponse>builder()
                                .result(result)
                                .message("Course change status unpublish successfully")
                                .build();
        }

        @Operation(summary = "Lấy lịch sử hoạt động khóa học", description = "Trả về danh sách hoạt động của khóa học")
        @GetMapping("/{courseId}/activities")
        public ApiResponse<List<CourseActivityResponse>> getCourseActivities(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                return ApiResponse.<List<CourseActivityResponse>>builder()
                                .result(courseActivityService.getAllActivities(courseId))
                                .message("Lấy lịch sử hoạt động thành công")
                                .build();
        }

        @Operation(summary = "Cập nhật giảm giá", description = "Cập nhật phần trăm giảm giá và thời hạn giảm giá cho khóa học")
        @PutMapping("/{courseId}/discount")
        public ApiResponse<CourseDetailResponse> updateDiscount(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "Dữ liệu giảm giá", required = true) @RequestBody UpdateDiscountRequest request) {
                return ApiResponse.<CourseDetailResponse>builder()
                                .result(courseService.updateDiscount(courseId, request.getDiscountPercent(), request.getDiscountEndTime()))
                                .message("Cập nhật giảm giá thành công")
                                .build();
        }

        @Operation(summary = "Cập nhật điểm đạt bài kiểm tra", description = "Cập nhật điểm yêu cầu để vượt qua bài kiểm tra")
        @PutMapping("/{courseId}/tests/{testId}/pass-score")
        public ApiResponse<Void> updateTestPassScore(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "ID bài kiểm tra", required = true) @PathVariable Long testId,
                        @Parameter(description = "Dữ liệu điểm đạt", required = true) @RequestBody UpdateTestPassScoreRequest request) {
                courseService.updateTestPassScore(courseId, testId, request.getPassScore());
                return ApiResponse.<Void>builder()
                                .message("Cập nhật điểm đạt thành công")
                                .build();
        }

}
