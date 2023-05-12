package com.gumi.enjoytrip.domain.recuriment.repository;

import com.gumi.enjoytrip.domain.recuriment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

}
