package com.example.basketball.controller;

import com.example.basketball.model.Player;
import com.example.basketball.enums.PlayerPosition;
import com.example.basketball.record.PlayerFailedPayload;
import com.example.basketball.record.PlayerSuccessPayload;
import com.example.basketball.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PlayerController {


    private final int MAXIMUM_CAPACITY = 12;


    @Autowired
    private PlayerRepository playerRepository;


    public PlayerController(PlayerRepository repository){
        this.playerRepository = repository;

    }

    @QueryMapping
    Iterable<Player> getAllPlayers(){
        return playerRepository.findAll();
    }

    @MutationMapping
    Object AddPlayer(@Argument PlayerInput player){

        if(playerRepository.count() >= MAXIMUM_CAPACITY)
            return new PlayerFailedPayload("maximum number of players reached (" + MAXIMUM_CAPACITY + ")! Please delete players before adding more." );

        if(!PlayerPosition.isValidPosition(player.position()))
            return new PlayerFailedPayload("Invalid Player Position, The valid positions are: {'PG','SG','SF','PF','C'}");

        if(player.name().isEmpty() || player.surname().isEmpty())
            return new PlayerFailedPayload("Name or surname cannot be empty");


        Player p = new Player(player.name(),player.surname(),player.position());
        return new PlayerSuccessPayload("A new player was added successfully." , playerRepository.save(p));
    }

    @MutationMapping
    Object DeletePlayer(@Argument Long id) {
        Optional<Player> player = playerRepository.findById(id);

        if(player.isEmpty())
            return new PlayerFailedPayload("player with id " + id + " does not exist!");
        playerRepository.deleteById(id);
        return new PlayerSuccessPayload("Player with id " + id + " was deleted successfully",player.get());


    }

    record PlayerInput(String name, String surname, String position){}
}
