package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.LectureRequest;
import com.cardano_lms.server.DTO.Response.LectureResponse;
import com.cardano_lms.server.DTO.Response.LectureSummaryResponse;
import com.cardano_lms.server.Entity.Lecture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LectureMapper {
    @Mapping(target = "previewFree", defaultValue = "false")
    @Mapping(target = "time", source = "duration")
    @Mapping(target = "description", source = "description")
    Lecture toEntity(LectureRequest request);

    @Mapping(target = "time", source = "duration")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    void updateLecture(LectureRequest request, @MappingTarget Lecture lecture);
    
    LectureResponse toResponse(Lecture newLecture);
    LectureSummaryResponse toSummaryResponse(Lecture lecture);
    List<LectureSummaryResponse> toSummaryResponseList(List<Lecture> lectures);
}
