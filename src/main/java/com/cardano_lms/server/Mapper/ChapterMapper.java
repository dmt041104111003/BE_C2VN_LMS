package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.ChapterRequest;
import com.cardano_lms.server.DTO.Response.ChapterResponse;
import com.cardano_lms.server.DTO.Response.ChapterSummaryResponse;
import com.cardano_lms.server.Entity.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = { LectureMapper.class, TestMapper.class })
public interface ChapterMapper {
    @Mapping(target = "lectures", ignore = true)
    @Mapping(target = "tests", ignore = true)
    Chapter toEntity(ChapterRequest request);

    @Mapping(target = "lectures", ignore = true)
    @Mapping(target = "tests", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    void updateChapter(ChapterRequest request, @MappingTarget Chapter chapter);

    ChapterResponse toResponse(Chapter chapter);
    
    ChapterSummaryResponse toSummaryResponse(Chapter chapter);
    List<ChapterSummaryResponse> toSummaryResponseList(List<Chapter> chapters);
}
