package com.cardano_lms.server.Utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryUtils {

    private final Cloudinary cloudinary;

    public CloudinaryUtils(
            @Value("${cloudinary.name}") String cloudName,
            @Value("${cloudinary.apikey}") String apiKey,
            @Value("${cloudinary.secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File không hợp lệ hoặc trống");
        }

        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", "image")
        );
    }

    public boolean deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            throw new IOException("publicId không hợp lệ");
        }
        Map result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "image")
        );
        return "ok".equals(result.get("result"));
    }
    public Map<String, Object> uploadImageFromBytes(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new IOException("File bytes không hợp lệ hoặc trống");
        }
        return cloudinary.uploader().upload(
                data,
                ObjectUtils.asMap("resource_type", "image")
        );
    }

    public Map<String, Object> uploadVideo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File không hợp lệ hoặc trống");
        }

        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "chunk_size", 6000000
                )
        );
    }

    public boolean deleteVideo(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            throw new IOException("publicId không hợp lệ");
        }
        Map result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "video")
        );
        return "ok".equals(result.get("result"));
    }
}
