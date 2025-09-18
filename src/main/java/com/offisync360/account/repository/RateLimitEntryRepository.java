package com.offisync360.account.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.RateLimitEntry;

@Repository
public interface RateLimitEntryRepository extends JpaRepository<RateLimitEntry, Long> {
    
    Optional<RateLimitEntry> findByIdentifierAndEndpointAndWindowStart(String identifier, String endpoint, LocalDateTime windowStart);
    
    @Query("SELECT rle FROM RateLimitEntry rle WHERE rle.identifier = :identifier AND rle.endpoint = :endpoint AND rle.windowStart <= :now AND rle.windowEnd >= :now")
    Optional<RateLimitEntry> findActiveEntry(@Param("identifier") String identifier, @Param("endpoint") String endpoint, @Param("now") LocalDateTime now);
    
    @Query("DELETE FROM RateLimitEntry rle WHERE rle.windowEnd < :expiredBefore")
    void deleteExpiredEntries(@Param("expiredBefore") LocalDateTime expiredBefore);
}