package com.owndeck.taskmanager.repository;

import com.owndeck.taskmanager.model.Task;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select t from Task t where t.project.id in :projectIds")
    List<Task> findByProjectIds(@Param("projectIds") List<Long> projectIds);

    @Query("select t from Task t where t.project.id = :projectId")
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    @Modifying
    @Query("delete from Task t where t.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    @Query("select t from Task t where t.assignedTo.id = :userId")
    List<Task> findByAssignedToId(@Param("userId") Long userId);

    @Query("select count(t) from Task t where t.assignedTo.id = :userId and t.status <> com.owndeck.taskmanager.model.TaskStatus.DONE and t.dueDate < :today")
    long countOverdueForUser(@Param("userId") Long userId, @Param("today") LocalDate today);
}
