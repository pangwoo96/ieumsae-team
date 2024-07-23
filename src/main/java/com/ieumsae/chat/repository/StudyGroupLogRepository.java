package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.StudyGroupLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyGroupLogRepository extends JpaRepository<StudyGroupLog, Long> {

    // 1:1, 그룹 채팅 chatIdx를 생성할 때 userIdx 반환 (스터디 방장)
    @Query("SELECT s.userIdx FROM StudyGroupLog s WHERE s.studyIdx = :studyIdx")
    Optional<Integer> findUserIdxByStudyIdx(Integer studyIdx);

    // 그룹 채팅방 입장 시, 유저가 스터디의 구성원인지 확인하는 로직
    Optional<StudyGroupLog> findByUserIdxAndStudyIdx(Integer userIdx, Integer studyIdx);

}
