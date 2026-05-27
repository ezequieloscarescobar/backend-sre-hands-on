package com.meli.streaming.repository;

import com.meli.streaming.entity.StreamingSession;
import com.meli.streaming.enums.StreamingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamingSessionRepository extends JpaRepository<StreamingSession, String> {

    List<StreamingSession> findByUserId(String userId);

    List<StreamingSession> findByUserIdAndStatusIn(String userId, List<StreamingStatus> statuses);
}
