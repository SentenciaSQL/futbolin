package com.futbolin.data.repository;

import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.domain.ranking.Division;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
    Page<UserProfileEntity> findAllByOrderByRankingPointsDesc(Pageable pageable);
    Page<UserProfileEntity> findByCountryOrderByRankingPointsDesc(String country, Pageable pageable);
    List<UserProfileEntity> findTop20ByDivisionOrderByRankingPointsDesc(Division division);
}
