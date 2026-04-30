package com.owndeck.taskmanager.repository;

import com.owndeck.taskmanager.model.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @EntityGraph(attributePaths = {"owner", "members", "members.user"})
    @Query("select distinct p from Project p left join p.members pm where p.owner.id = :userId or pm.user.id = :userId")
    List<Project> findAccessibleProjects(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"owner", "members", "members.user"})
    Optional<Project> findWithDetailsById(Long id);
}
