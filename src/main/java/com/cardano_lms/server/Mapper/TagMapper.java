package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.TagRequest;
import com.cardano_lms.server.DTO.Response.TagResponse;
import com.cardano_lms.server.Entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {
    Tag toTag(TagRequest tagRequest);

    @Mapping(target = "numOfCourses", expression = "java(tag.getCourses() != null ? tag.getCourses().size() : 0)")
    TagResponse toTagResponse(Tag tag);
}
