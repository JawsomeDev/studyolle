package com.studyolle.event;

import com.studyolle.domain.Event;
import com.studyolle.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"enrollments"})
    List<Event> findByStudyOrderByStartDateTime(Study study);
}
