package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.ContactMessageRequest;
import com.cardano_lms.server.DTO.Response.ContactMessageResponse;
import com.cardano_lms.server.Entity.ContactMessage;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactMessageService {
    private final ContactMessageRepository contactMessageRepository;

    
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ContactMessageResponse submitMessage(ContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .email(request.getEmail())
                .content(request.getContent())
                .build();
        
        return toResponse(contactMessageRepository.save(message));
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContactMessageResponse> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContactMessageResponse> getUnreadMessages() {
        return contactMessageRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    public long getUnreadCount() {
        return contactMessageRepository.countByIsReadFalse();
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ContactMessageResponse markAsRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        message.setRead(true);
        return toResponse(contactMessageRepository.save(message));
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteMessage(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        contactMessageRepository.deleteById(id);
    }

    private ContactMessageResponse toResponse(ContactMessage message) {
        return ContactMessageResponse.builder()
                .id(message.getId())
                .email(message.getEmail())
                .content(message.getContent())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
