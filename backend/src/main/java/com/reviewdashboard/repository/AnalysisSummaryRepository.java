package com.reviewdashboard.repository;

import com.reviewdashboard.entity.AnalysisSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisSummaryRepository extends JpaRepository<AnalysisSummary, Long> {
    Optional<AnalysisSummary> findByBatchId(String batchId);
}
