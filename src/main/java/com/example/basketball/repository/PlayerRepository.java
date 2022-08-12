package com.example.basketball.repository;

import com.example.basketball.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player,Long> {


    @Query(value = "SELECT player_id FROM player ORDER BY player_id DESC LIMIT 1",nativeQuery = true)
    long getLastId();


}
