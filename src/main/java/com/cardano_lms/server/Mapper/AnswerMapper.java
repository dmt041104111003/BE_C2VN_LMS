package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.AnswerRequest;
import com.cardano_lms.server.DTO.Response.AnswerResponse;
import com.cardano_lms.server.Entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    @Mapping(target = "correct", source = "correct")
    Answer toAnswer(AnswerRequest request);

    default AnswerResponse toResponse(Answer answer) {
        if (answer == null) return null;
        return AnswerResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .correct(answer.isCorrect())
                .build();
    }
    
    default List<AnswerResponse> toResponseList(List<Answer> answers) {
        if (answers == null) return null;
        return answers.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    void updateAnswer(AnswerRequest aReq, @MappingTarget Answer ans);
}
