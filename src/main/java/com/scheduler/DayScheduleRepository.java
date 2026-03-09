package com.scheduler;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayScheduleRepository extends JpaRepository<DaySchedule, Long> {

	List<DaySchedule> findByUserIdAndWeekdayOrderByStartTimeAsc(Long userId, Integer weekday);
	// ユーザーごとの予定を取得するメソッド例
	//List<DaySchedule> findByUserId(Long userId);
}
