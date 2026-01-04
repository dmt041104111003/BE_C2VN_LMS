package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionsResponse {
    private List<CourseItem> courses;
    private List<InstructorItem> instructors;
    private List<TagItem> tags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseItem {
        private String id;
        private String title;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorItem {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagItem {
        private Long id;
        private String name;
        private String slug;
    }
}
