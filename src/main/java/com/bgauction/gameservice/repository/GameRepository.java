package com.bgauction.gameservice.repository;

import com.bgauction.gameservice.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findAllByUserId(Long longs);
}
