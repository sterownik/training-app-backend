package com.example.training.repository;

import com.example.training.model.Activity;
import com.example.training.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findAllByUser(User user);

    Optional<Activity> findFirstByUserIdOrderByStartDateLocalDesc(Long userId);

    boolean existsByStravaActivityId(Long stravaActivityId);

    List<Activity> findByUserIdAndTypeOrderByStartDateLocalDesc(
            Long userId,
            String type,
            Pageable pageable
    );

    Page<Activity> findByUserIdOrderByStartDateLocalDesc(
            Long userId,
            Pageable pageable
    );

    List<Activity> findByUserIdOrderByStartDateLocalDesc(
            Long userId
    );

    // Pobiera 50 najnowszych aktywności użytkownika, które NIE MAJĄ jeszcze przypisanych okrążeń
    List<Activity> findFirst60ByUserIdOrderByStartDateLocalDesc(Long userId);


    @NativeQuery("SELECT * FROM ACTIVITIES WHERE USER_ID = ?1 AND START_DATE_LOCAL >= ?2 AND START_DATE_LOCAL <= ?3 order by START_DATE_LOCAL desc ")
    List<Activity> findByUserIdAndStartByStartDateLocalAndEnd(
            Long userId,
            Date startDateLocalStart,
            Date startDateLocalEnd
    );



    Activity findByIdAndUserId(Long id, Long userId);
}