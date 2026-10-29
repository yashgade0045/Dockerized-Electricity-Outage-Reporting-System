package com.example.outage.repository;

import com.example.outage.model.OutageReport;
import com.example.outage.model.OutageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link OutageReport}.
 * Provides CRUD, pagination, and custom search queries backed by the configured database.
 */
@Repository
public interface OutageReportRepository extends JpaRepository<OutageReport, Long> {

    /**
     * Count reports by a specific status.
     * Used for dashboard statistics.
     */
    long countByStatus(OutageStatus status);

    /**
     * Find all reports matching a given status, with pagination.
     */
    Page<OutageReport> findByStatus(OutageStatus status, Pageable pageable);

    /**
     * Full-text search across title, location, and description.
     * Supports the search bar and filter panel.
     *
     * @param keyword  search term (case-insensitive)
     * @param pageable pagination parameters
     */
    @Query("SELECT r FROM OutageReport r WHERE " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.reporterName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<OutageReport> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search within a specific status.
     */
    @Query("SELECT r FROM OutageReport r WHERE " +
           "r.status = :status AND (" +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<OutageReport> searchByKeywordAndStatus(@Param("keyword") String keyword,
                                                 @Param("status") OutageStatus status,
                                                 Pageable pageable);
}
