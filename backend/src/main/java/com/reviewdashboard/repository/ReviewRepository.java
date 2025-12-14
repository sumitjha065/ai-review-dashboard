package com.reviewdashboard.repository;

import com.reviewdashboard.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBatchId(String batchId);

    Page<Review> findByBatchId(String batchId, Pageable pageable);

    @Query("SELECT r.sentiment, COUNT(r) FROM Review r WHERE r.batchId = :batchId GROUP BY r.sentiment")
    List<Object[]> countSentimentByBatchId(String batchId);
}
