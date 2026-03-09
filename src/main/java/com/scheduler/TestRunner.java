package com.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

//@Component //テストをするときは@Componentのコメントを外してください
public class TestRunner implements CommandLineRunner{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ScheduleService scheduleService;
	 
	@Override
	public void run (String... args) throws Exception {
		System.out.println("テスト開始");
		 
		 
		try {
			if (!userRepository.existsById(1L)) {
				User dummyUser = new User();
				// idは自動採番（IDENTITY）なのでセットしなくても良いですが、
				// もしIDを指定して作りたい場合はDBの状態を確認してください。
				// 今回はシンプルに「誰でもいいからユーザーがいる状態」を作ります。
				dummyUser.setName("テストユーザー");
				dummyUser.setPasswordHash("password");
				userRepository.save(dummyUser);
				System.out.println("テスト用ユーザーを作成しました。");
			}
			 
			scheduleService.addSchedule(1L, 1L, "睡眠", "#0000FF", 1, scheduleService.timeToMinutes("00:00"), scheduleService.timeToMinutes("06:00"));
			 
			List<DaySchedule> list = scheduleService.getScheduleByWeekday(1L, 1);
			 
			if (list.isEmpty()) {
				System.out.println("データ取得失敗");
			} else {
				for (DaySchedule ds : list) {
					System.out.println("予定：" + ds.getCategoryName());
					System.out.println("開始（分）：" + ds.getStartTime());
					System.out.println("終了（分）：" + ds.getEndTime());
				}
			}
		} catch (Exception e) {
			System.out.println("テストで例外が発生しました");
			e.printStackTrace();
		}
	}
}
