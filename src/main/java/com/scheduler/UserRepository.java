package com.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository

//ユーザー情報のリポジトリ
public interface UserRepository extends JpaRepository<User, Long>{
	User findByName(String name);
}
