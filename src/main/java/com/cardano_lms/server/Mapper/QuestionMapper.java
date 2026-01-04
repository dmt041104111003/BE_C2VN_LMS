package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.QuestionRequest;
import com.cardano_lms.server.DTO.Response.QuestionResponse;
import com.cardano_lms.server.Entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = AnswerMapper.class)
public interface QuestionMapper {
    @Mapping(target = "test", ignore = true)
    @Mapping(target = "answers", ignore = true)
    Question toQuestion(QuestionRequest request);

    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "test", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateQuestion(QuestionRequest request, @MappingTarget Question question);

    QuestionResponse toResponse(Question question);
    List<QuestionResponse> toResponseList(List<Question> questions);
}