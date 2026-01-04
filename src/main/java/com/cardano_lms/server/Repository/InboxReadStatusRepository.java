package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.InboxReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface InboxReadStatusRepository extends JpaRepository<InboxReadStatus, Long> {
    
    Optional<InboxReadStatus> findByUserIdAndItemTypeAndItemId(String userId, String itemType, Long itemId);
    
    boolean existsByUserIdAndItemTypeAndItemId(String userId, String itemType, Long itemId);
    
    @Query("SELECT r.itemId FROM InboxReadStatus r WHERE r.user.id = :userId AND r.itemType = :itemType")
    Set<Long> findReadItemIdsByUserIdAndItemType(@Param("userId") String userId, @Param("itemType") String itemType);
}





