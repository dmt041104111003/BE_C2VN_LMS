package com.cardano_lms.server.Service;



import com.cardano_lms.server.Entity.Media;
import com.cardano_lms.server.constant.MediaType;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.MediaRepository;
import com.cardano_lms.server.Utils.CloudinaryUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final CloudinaryUtils cloudinaryUtils;
    private final MediaRepository mediaRepository;


    @PreAuthorize("hasRole('ADMIN')")
    public Media upload(MultipartFile file, MediaType type , String title, String description, String location) throws IOException {
        Map<String, Object> uploadResult = cloudinaryUtils.uploadImage(file);

        Media media = Media.builder()
                .title(title)
                .description(description)
                .location(location)
                .url(uploadResult.get("url").toString())
                .publicId(uploadResult.get("public_id").toString())
                .type(type)
                .build();

        return mediaRepository.save(media);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public boolean delete(String publicId) throws IOException {
        boolean success = cloudinaryUtils.deleteImage(publicId);
        if (success) {
            Media media = mediaRepository.findByPublicId(publicId);
            if (media != null) {
                mediaRepository.delete(media);
            }
        }
        return success;
    }

    public List<Media> getAll(MediaType type) {
        return mediaRepository.findByType(type);
    }

    public void uploadFromBytes(byte[] data, Integer orderIndex, MediaType type, String title, String desc, String location,String link) throws IOException {
        Map<String, Object> uploadResult = cloudinaryUtils.uploadImageFromBytes(data);

        Media media = Media.builder()
                .title(title)
                .description(desc)
                .orderIndex(orderIndex)
                .location(location)
                .url(uploadResult.get("secure_url").toString())
                .publicId(uploadResult.get("public_id").toString())
                .link(link)
                .type(type)
                .build();

        mediaRepository.save(media);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Media editMedia(Long id, MultipartFile file, MediaType type, String title, String description, String location,String link) throws IOException {

        Media existing = mediaRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.MEDIA_NOT_FOUND));
        if (existing.getPublicId() != null && !existing.getPublicId().isEmpty()) {
            cloudinaryUtils.deleteImage(existing.getPublicId());
        }

        if(!file.isEmpty()){
            Map<String, Object> uploadResult = cloudinaryUtils.uploadImage(file);
            existing.setUrl(uploadResult.get("secure_url").toString());
            existing.setPublicId(uploadResult.get("public_id").toString());
        }

        existing.setTitle(title != null ? title : existing.getTitle());
        existing.setDescription(description != null ? description : existing.getDescription());
        existing.setLocation(location != null ? location : existing.getLocation());
        existing.setType(type != null ? type : existing.getType());
        existing.setLink(link != null ? link : existing.getLink());


        return mediaRepository.save(existing);
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public Map<String, Object> uploadVideo(MultipartFile file) throws IOException {
        return cloudinaryUtils.uploadVideo(file);
    }

}
