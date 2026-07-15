package com.example.training.repository;

import com.example.training.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage>
    findTop10ByIdChatOrderByCreatedAtDesc(UUID idChat);


    @Query("""
    SELECT cm
    FROM ChatMessage cm
    WHERE cm.idChat = (
        SELECT c.idChat
        FROM ChatMessage c
        WHERE c.userId = :userId
        AND c.createdAt = (
            SELECT MAX(c2.createdAt)
            FROM ChatMessage c2
            WHERE c2.userId = :userId
        )
    )
    ORDER BY cm.createdAt ASC
""")
    List<ChatMessage> findLatestChatMessages(
            @Param("userId") Long userId
    );
}