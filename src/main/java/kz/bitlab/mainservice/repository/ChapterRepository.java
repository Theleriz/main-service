package kz.bitlab.mainservice.repository;

import kz.bitlab.mainservice.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findAllByOrderByIdAsc();

    List<Chapter> findAllByCourseIdOrderByOrderAsc(Long courseId);
}
