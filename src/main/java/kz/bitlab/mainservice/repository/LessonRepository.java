package kz.bitlab.mainservice.repository;

import kz.bitlab.mainservice.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByOrderByIdAsc();

    List<Lesson> findAllByChapterIdOrderByOrderAsc(Long chapterId);
}
