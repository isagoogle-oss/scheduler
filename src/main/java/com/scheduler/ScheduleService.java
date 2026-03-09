package com.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
	
	@Autowired private DayScheduleRepository dayScheduleRepository;
	@Autowired private UserRepository userRepository; 
	
	
	//ver1.1対応　各々のメソッドで使われていたweekNamesを定数で宣言
	private final String[] weekNames = {"月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日"};
	/**
	 * ログイン認証
	 */
	public User login(String name, String passwordHash) {
		User user = userRepository.findByName(name);
		if (user != null && user.getPasswordHash().equals(passwordHash)) {
			return user;
		}
		return null;
	}
	
	
	/**
	 * ユーザー登録処理：UserクラスのEntityを生成
	 */
	//ver1.1対応　戻り値をboolean型にし同名ユーザーIDでの登録を不可とする
	public boolean registerUser(String name, String passwordHash) {
		// 1. 同名のユーザーが既に存在するかチェック（保管場所へ確認）
		if (userRepository.findByName(name) != null) {
			// すでに名前が使われている場合は false を返す
			return false;
		}
		// 2. 存在しない場合は新規登録
		User user = new User();
		user.setName(name);
		user.setPasswordHash(passwordHash);
		userRepository.save(user);
		return true; 
	}
	
	
	/*ver1.1対応によりコメント化
	public void registerUser(String name, String passwordHash) {
		User user = new User();
		user.setName(name);
		user.setPasswordHash(passwordHash);
		userRepository.save(user);
	}*/
	
	
	public List<DaySchedule> getScheduleByWeekday(Long userId, Integer weekday) {
		return dayScheduleRepository.findByUserIdAndWeekdayOrderByStartTimeAsc(userId, weekday);
	}

	
	/**
	 * マイページ表示用データ：Map形式で一括作成してコントローラーに渡す
	 */
	public Map<String, Object> getMyPageData(User user, Integer week) {
		// 曜日指定がない場合は今日、ある場合は指定の曜日を選択
		int displayDay = (week != null) ? week : java.time.LocalDate.now().getDayOfWeek().getValue() - 1;
		
		Map<String, Object> data = new HashMap<>();
		data.put("schedules", getScheduleByWeekday(user.getId(), displayDay));
		data.put("selectedWeekday", displayDay);
		data.put("weekNames", weekNames); //ver1.1対応
		//new String[]{"月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日"});
		return data;
	}
	
	
	/**
	 * 編集画面用データ：画面表示に必要な定数などを一括作成
	 */
	//ver1.1対応
	public Map<String, Object> getEditPageData(Long userId, Integer week, Long id) {
	/*public Map<String, Object> getEditPageData(Integer week) {
		String[] weekNames = {"月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日"};//ver1.1対応 */
		Map<String, Object> data = new HashMap<>();
		
		//ver1.1対応 IDがある場合はDBから取得、ない場合は新規作成用Entityを準備
		DaySchedule schedule = (id != null) ? dayScheduleRepository.findById(id).orElse(new DaySchedule()) : new DaySchedule();
		
		//ver1.1対応 
		int selectedWeek = (id != null && schedule.getWeekday() != null) ? schedule.getWeekday() : (week != null ? week : 0);
		data.put("schedule", schedule);
		data.put("selectedWeek", selectedWeek);
		data.put("selectedWeekName", weekNames[selectedWeek]);
		// 編集画面の下に一覧を出すためのデータ
		data.put("schedules", getScheduleByWeekday(userId, selectedWeek));
		
		// 既存予定がある場合、プルダウン選択用の時・分を計算
		if (id != null && schedule.getStartTime() != null) {
			data.put("startHour", schedule.getStartTime() / 60);
			data.put("startMin", schedule.getStartTime() % 60);
			data.put("endHour", schedule.getEndTime() / 60);
			data.put("endMin", schedule.getEndTime() % 60);
		}
		
		/*ver1.1対応 
		data.put("selectedWeek", week);
		data.put("selectedWeekName", weekNames[week]);*/
		
		return data;
	}
	
	/**
	 * DB登録用：新しい予定を登録
	 */
	//ver1.1対応 引数にLong idを追加
	public void addSchedule (Long id, Long userId, String category, String color, Integer weekday, Integer startTime, Integer endTime) {
		
		//ver1.1対応 idがあれば既存を読み込み、なければ新規
		DaySchedule schedule = (id != null) ? dayScheduleRepository.findById(id).orElse(new DaySchedule()) : new DaySchedule();
		
		//DaySchedule schedule = new DaySchedule();
		schedule.setUserId(userId); 
		schedule.setCategoryName(category); 
		schedule.setCategoryColor(color);
		schedule.setWeekday(weekday); 
		schedule.setStartTime(startTime); 
		schedule.setEndTime(endTime);
		dayScheduleRepository.save(schedule);
	}
	
	/**
	 * 取得時間変換ロジック：DBへ登録するために〇〇:〇〇形式の文字列を分単位の形式に変換する
	 */
	public Integer timeToMinutes(String timeStr) {
		if (timeStr == null || !timeStr.contains(":")) return 0;
		String[] time = timeStr.split(":");
		return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
	}
	
	/**
	 * 予定の重複チェック：DBに予定を追加する前に時間が重複するかを確認する
	 */
	//ver1.1対応 引数にLong idを追加
	public boolean isOverlapping(Long id, Long userId, Integer weekday, Integer startTime, Integer endTime) {
		List<DaySchedule> existingSchedules = getScheduleByWeekday(userId, weekday);
		for (DaySchedule existing : existingSchedules) {

			//ver1.1対応 更新時、自分自身のデータとの重複判定はスキップする
			if (id != null && existing.getId().equals(id)) continue;

			if (existing.getStartTime() < endTime && startTime < existing.getEndTime()) return true;
		}
		return false;
	}
	
	
	/**
	 * 予定の保存実行：時間の前後チェックや重複チェックを含む
	 * @return エラーがある場合はerr:エラーメッセージ、成功時はok:処理完了メッセージとする
	 */
	//ver1.1対応 引数にLong idを追加 エラーメッセージの冒頭にerr:処理完了時にメッセージを表示するためにok:処理完了メッセージを追加
	public String trySaveSchedule(Long id, Long userId, Integer weekday, String category, String color, String start, String end) {
		Integer startTime = timeToMinutes(start);
		Integer endTime = timeToMinutes(end);
		
		//0分の予定や開始時刻が終了時刻より後に設定した場合エラーメッセージを表示
		if (startTime >= endTime) return "err:終了時刻は開始時刻より後に設定してください。";
		
		//重複チェックのメソッドを呼び出し、重複時にエラーメッセージを表示
		if (isOverlapping(id, userId, weekday, startTime, endTime)) return "err:その時間帯には既に他の予定が入っています。";
		
		addSchedule(id, userId, category, color, weekday, startTime, endTime);
		return "ok:予定を保存しました。";
	}
	
	
	/**
	 * 予定の削除実行（ver1.1追加）
	 */
	public void deleteSchedule(Long id) {
		dayScheduleRepository.deleteById(id);
	}
}