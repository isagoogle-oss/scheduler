package com.scheduler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ScheduleController {

	@Autowired private ScheduleService scheduleService; // RepositoryはAutowiredしない（サービス経由で操作）

	
	// --- ログイン/ログアウト ---
	@GetMapping("/")
	public String showLogin() {
		return "index"; 
		}
	
	
	// ログイン判定
	@PostMapping("/login")
	public String processLogin(@RequestParam String name, @RequestParam String passwordHash,
			HttpServletRequest request, RedirectAttributes ra) {
		
		// 1. ログイン前に古いセッションを破棄（セッション固定攻撃の防止）
		HttpSession session = request.getSession(false);
		if (session != null) session.invalidate();
		
		// 2. ログイン判定はScheduleService.java側で行う
		User user = scheduleService.login(name, passwordHash);
		
		if (user != null) {
			// 3. 新しいセッションを作成(true)
			// 変数 newSession に格納することで、この後のセット処理の対象が新規セッションであることを明確にする
			HttpSession newSession = request.getSession(true);
			newSession.setAttribute("user", user); // 認証済みユーザー情報を新しいセッションに格納
			return "redirect:/mypage";  // 成功時のリダイレクト
		}
		// 4. ログイン失敗時はエラーメッセージをセットしてログイン画面へ戻す
		ra.addFlashAttribute("errorMessage", "ユーザー名またはパスワードが正しくありません。");
		return "redirect:/";
	}
	
	
	// ログアウト
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // セッションを無効化（クリア）
		return "redirect:/";  // ログイン画面へ戻す
	}
	
	
	// --- ユーザー登録 ---
	@GetMapping("/signup")
	public String signupForm() {
		return "signup"; 
	}
	
	
	//ユーザー情報の登録
	@PostMapping("/signup")
	//ver1.1対応 リダイレクト用の引数を追加
	public String signup(@RequestParam String name, @RequestParam String passwordHash, RedirectAttributes ra) {
		// 登録処理はScheduleService.java側で行う
		//scheduleService.registerUser(name, passwordHash); //ver1.1対応のためコメントアウト

		//ver1.1対応 新規登録でのユーザーID重複チェック
		if (!scheduleService.registerUser(name, passwordHash)) {
            ra.addFlashAttribute("errorMessage", "そのユーザー名は既に使用されています。");
            return "redirect:/signup";
        }
		return "redirect:/";
	}
	
	
	// --- マイページ ---
	@GetMapping("/mypage")
	public String showMyPage(@RequestParam(required = false) Integer week, HttpSession session, Model model) {
		
		// 1. セッションからログインユーザーを取得
		User user = (User) session.getAttribute("user");
		if (user == null) return "redirect:/"; 
		
		// 2. サービス側から受け取ったMapデータを一括でModelに追加
		model.addAllAttributes(scheduleService.getMyPageData(user, week));
		return "mypage";
	}
	
	
	// --- 編集 ---
	@GetMapping("/edit")
	//ver1.1対応 
	public String showScheduleEditor(@RequestParam(required = false) Long id, 
                               @RequestParam(required = false) Integer week, 
                               HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/";
	//public String showScheduleEditor(@RequestParam Integer week, Model model) {//ver1.1対応のためコメントアウト

		// 画面に必要なデータをサービス側から取得
		//model.addAllAttributes(scheduleService.getEditPageData(week)); //ver1.1対応のためコメントアウト

		//ver1.1対応 新規編集両方のデータを準備する
		model.addAllAttributes(scheduleService.getEditPageData(user.getId(), week, id));
		return "edit";
	}
	
	
	/**
	 * 予定の保存処理（登録ボタン押下時）
	 * 重複チェックなどの実務ロジックはすべてサービス側で行う
	 */
	@PostMapping("/edit")
	//ver1.1対応 @RequestParam(required = false) Long idを追加
	public String saveSchedule(@RequestParam(required = false) Long id,		
			@RequestParam Integer weekday, 
			@RequestParam String categoryName,
			@RequestParam String categoryColor, 
			@RequestParam String startTimeStr,
			@RequestParam String endTimeStr, 
			HttpSession session, 
			RedirectAttributes ra) {
		
		// 1. セッションからログイン中のユーザー情報を取得
		User user = (User) session.getAttribute("user");
		if (user == null) return "redirect:/";
		
		// 2. 時間の変換、前後関係のチェック、既存予定との重複チェック、保存処理をサービス側で行う
		//ver1.1対応 idを追加 処理完了のメッセージを追加するため制御を変更
		//String errorMessage = scheduleService.trySaveSchedule(
		String result = scheduleService.trySaveSchedule(
				id, user.getId(), weekday, categoryName, categoryColor, startTimeStr, endTimeStr);
				
		// 3. サービス側からバリデーション失敗と返ってきた場合、メッセージをフラッシュ属性にセットし、入力中の曜日を維持したまま編集画面へ戻す。
		//if (errorMessage != null) {
		if (result.startsWith("err:")) {
			ra.addFlashAttribute("errorMessage", result.substring(4));
			
			//ver1.1対応
			//return "redirect:/edit?week=" + weekday;
			return "redirect:/edit?week=" + weekday + (id != null ? "&id=" + id : "");
		}
		
		// 4. 保存成功の場合は、登録した曜日の編集画面(edit.html)へリダイレクト
		//ver1.1対応 
		ra.addFlashAttribute("successMessage", result.substring(3));
		return "redirect:/edit?week=" + weekday;
	}
	
	
	/**
	 * 削除機能
	 */
	//ver1.1対応
	@PostMapping("/delete")
	public String delete(@RequestParam Long id, @RequestParam Integer weekday) {
		scheduleService.deleteSchedule(id);
		//ver1.1対応 
		return "redirect:/edit?week=" + weekday;
	}
}