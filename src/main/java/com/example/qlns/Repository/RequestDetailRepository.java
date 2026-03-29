package com.example.qlns.Repository;

import com.example.qlns.Entity.RequestDetail;
import com.example.qlns.Enum.LeaveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RequestDetailRepository extends JpaRepository<RequestDetail, Long> {

    @Query("SELECT COUNT(rd) > 0 FROM RequestDetail rd " +
           "WHERE rd.request.employee.id = :employeeId " +
           "AND rd.specificDate = :date " +
           "AND rd.request.status IN :statuses " +
           "AND (rd.leaveSession = com.example.qlns.Enum.LeaveSession.ALL_DAY " +
           "     OR :session = com.example.qlns.Enum.LeaveSession.ALL_DAY " +
           "     OR rd.leaveSession = :session)")
    boolean existsOverlappingLeave(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("session") LeaveSession session,
            @Param("statuses") java.util.List<com.example.qlns.Enum.RequestStatus> statuses);
}
