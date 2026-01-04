package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.FeedbackRequest;
import com.cardano_lms.server.DTO.Response.FeedbackResponse;
import com.cardano_lms.server.Entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replies", ignore = true)
    Feedback toFeedback(FeedbackRequest feedbackRequest);

    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userWalletAddress", source = "user.walletAddress")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "replies", source = "replies")
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "dislikeCount", ignore = true)
    @Mapping(target = "userReaction", ignore = true)
    FeedbackResponse toFeedbackResponse(Feedback feedback);

    List<FeedbackResponse> toFeedbackResponses(List<Feedback> feedbacks);
}
