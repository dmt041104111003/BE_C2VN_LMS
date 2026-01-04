package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.TagRequest;
import com.cardano_lms.server.DTO.Response.TagResponse;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.Tag;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.TagMapper;
import com.cardano_lms.server.Repository.CourseRepository;
import com.cardano_lms.server.Repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    private String generateSlug(String input) {
        if (input == null) return null;
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }


    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toTagResponse)
                .collect(Collectors.toList());
    }







    @PreAuthorize("hasRole('INSTRUCTOR')")
    public TagResponse createTag(TagRequest tagRequest) {
        if(tagRepository.existsBySlug(generateSlug(tagRequest.getName()))){
            throw new AppException(ErrorCode.SLUG_HAS_BEEN_EXISTED);
        }
        Tag tag = tagMapper.toTag(tagRequest);
        return tagMapper.toTagResponse(tagRepository.save(tag));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteTag(Long id) {
        if(tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return;
        }
        throw new AppException(ErrorCode.TAG_NOT_FOUND);
    }


}
