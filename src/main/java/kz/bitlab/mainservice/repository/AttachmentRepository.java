package kz.bitlab.mainservice.repository;

import kz.bitlab.mainservice.entity.Attachment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByLessonIdOrderByIdAsc(Long lessonId);

    // Loads lesson -> chapter -> course in one query for the course access check
    @EntityGraph(attributePaths = "lesson.chapter.course")
    Optional<Attachment> findWithCourseById(Long id);
}
