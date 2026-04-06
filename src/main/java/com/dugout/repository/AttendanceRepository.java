package com.dugout.repository;

import com.dugout.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEventId(Long eventId);
    List<Attendance> findByPlayerIdAndEvent_TeamId(Long playerId, Long teamId);
    Optional<Attendance> findByPlayerIdAndEventId(Long playerId, Long eventId);
    List<Attendance> findByEvent_TeamId(Long teamId);
}
