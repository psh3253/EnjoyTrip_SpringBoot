package com.gumi.enjoytrip.domain.participant.repository;

import com.gumi.enjoytrip.domain.participant.dto.ParticipantListDto;
import com.gumi.enjoytrip.domain.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<ParticipantListDto> findAllByRecruitmentId(long id);
}
