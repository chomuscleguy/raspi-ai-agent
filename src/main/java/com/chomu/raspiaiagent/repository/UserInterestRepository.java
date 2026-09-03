package com.chomu.raspiaiagent.repository;

import com.chomu.raspiaiagent.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findByActiveTrue();

    Optional<UserInterest> findByKeywordIgnoreCase(String keyword);
}