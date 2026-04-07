package com.dugout.repository;

import com.dugout.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByTeamIdOrderByDateAsc(Long teamId);
    List<Event> findByTeamIdAndDateBetween(Long teamId, LocalDate startDate, LocalDate endDate);
}
