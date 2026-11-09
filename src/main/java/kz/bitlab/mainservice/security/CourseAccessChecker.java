package kz.bitlab.mainservice.security;

import kz.bitlab.mainservice.entity.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Checks the user's access level to a course before its content (lesson attachments) is given out.
 * Courses have no enrollments yet, so every user with ROLE_ADMIN, ROLE_TEACHER or ROLE_USER
 * may read every course; a user without any of these roles may not. Per-course rules
 * (enrollments, course teachers) belong here.
 */
@Slf4j
@Component
public class CourseAccessChecker {

    private static final Set<String> COURSE_READ_ROLES =
            Set.of(Role.ROLE_ADMIN.name(), Role.ROLE_TEACHER.name(), Role.ROLE_USER.name());

    public void checkReadAccess(Authentication authentication, Course course) {
        boolean allowed = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(COURSE_READ_ROLES::contains);
        if (!allowed) {
            log.warn("User {} has no access to course {}",
                    authentication != null ? authentication.getName() : null, course.getId());
            throw new AccessDeniedException("No access to course with id " + course.getId());
        }
    }
}
