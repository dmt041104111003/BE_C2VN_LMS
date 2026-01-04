package com.cardano_lms.server.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "inbox_read_status", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "item_type", "item_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboxReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_type", nullable = false)
    private String itemType; 

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}





