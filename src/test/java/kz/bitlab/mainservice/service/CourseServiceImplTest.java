package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.entity.Course;
import kz.bitlab.mainservice.mapper.CourseMapper;
import kz.bitlab.mainservice.repository.CourseRepository;
import kz.bitlab.mainservice.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static kz.bitlab.mainservice.TestData.COURSE_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void getAll_returnsAllCourses() {
        List<Course> courses = List.of(TestData.course());
        List<CourseDto> dtos = List.of(TestData.courseDto());
        when(courseRepository.findAllByOrderByIdAsc()).thenReturn(courses);
        when(courseMapper.toDtoList(courses)).thenReturn(dtos);

        List<CourseDto> result = courseService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo(TestData.COURSE_NAME);
        verify(courseRepository).findAllByOrderByIdAsc();
    }

    @Test
    void getById_existingId_returnsCourse() {
        Course course = TestData.course();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseMapper.toDto(course)).thenReturn(TestData.courseDto());

        CourseDto result = courseService.getById(COURSE_ID);

        assertThat(result.getId()).isEqualTo(COURSE_ID);
        assertThat(result.getName()).isEqualTo(TestData.COURSE_NAME);
    }

    @Test
    void getById_missingId_throwsNotFound() {
        when(courseRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.getById(MISSING_ID));

        assertThat(ex.getMessage()).isEqualTo("Course with id 999 not found");
    }

    @Test
    void create_savesAndReturnsCourse() {
        CourseCreateRequest request = TestData.courseCreateRequest();
        Course course = TestData.course();
        when(courseMapper.toEntity(request)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toDto(course)).thenReturn(TestData.courseDto());

        CourseDto result = courseService.create(request);

        assertThat(result.getId()).isEqualTo(COURSE_ID);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void update_existingId_updatesAndReturnsCourse() {
        CourseUpdateRequest request = TestData.courseUpdateRequest();
        Course course = TestData.course();
        CourseDto updatedDto = TestData.courseDto();
        updatedDto.setName(request.getName());
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(course)).thenReturn(course);
        when(courseMapper.toDto(course)).thenReturn(updatedDto);

        CourseDto result = courseService.update(COURSE_ID, request);

        assertThat(result.getName()).isEqualTo("Java Backend Developer");
        verify(courseMapper).updateEntity(request, course);
        verify(courseRepository).saveAndFlush(course);
    }

    @Test
    void update_missingId_throwsNotFoundAndDoesNotSave() {
        when(courseRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> courseService.update(MISSING_ID, TestData.courseUpdateRequest()));

        verify(courseRepository, never()).saveAndFlush(any());
    }

    @Test
    void delete_existingId_deletesCourse() {
        when(courseRepository.existsById(COURSE_ID)).thenReturn(true);

        courseService.delete(COURSE_ID);

        verify(courseRepository).deleteById(COURSE_ID);
    }

    @Test
    void delete_missingId_throwsNotFoundAndDoesNotDelete() {
        when(courseRepository.existsById(MISSING_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> courseService.delete(MISSING_ID));

        verify(courseRepository, never()).deleteById(anyLong());
    }
}
