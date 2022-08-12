package com.example.basketball;

import com.example.basketball.model.Player;
import com.example.basketball.record.PlayerFailedPayload;
import com.example.basketball.record.PlayerSuccessPayload;
import com.example.basketball.repository.PlayerRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureGraphQlTester
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlayerControllerIntTest {


    @Autowired
    GraphQlTester graphQlTester;

    @Autowired
    PlayerRepository repository;

    @Test
    @Order(1)
    void testGetAllPlayersShouldReturnAllPlayers() {

        int currentPlayerCount = (int) repository.count();

        // language=GraphQL
        String document = """
        query {
            getAllPlayers {
                id
                name
                surname
                position         
            }
        }    
        """;

        graphQlTester.document(document)
                .execute()
                .path("getAllPlayers")
                .entityList(Player.class)
                .hasSize(currentPlayerCount);

        // Clear the database to start the integration tests
        repository.deleteAll();
    }

    @Test
    @Order(2)
    void shouldCreateNewPlayer() {

        int currentPlayerCount = (int) repository.count();

        // language=graphQL
        String document = """
            mutation AddPlayer($name: String!, $surname: String!, $position: String!){
                AddPlayer(player: {name: $name, surname : $surname, position: $position }){
                    ... on PlayerSuccessPayload{
                        player {
                           id
                           name
                           surname
                           position                        
                        }
                    }
                }
            }
        """;

        AtomicLong id = new AtomicLong();
        graphQlTester.document(document)
                .variable("name","dogukan")
                .variable("surname","ozdemir")
                .variable("position","PG")
                .execute()
                .path("AddPlayer")
                .entity(PlayerSuccessPayload.class)
                .satisfies(playerSuccessPayload -> {
                    assertNotNull(playerSuccessPayload.player().getId());
                    assertEquals("dogukan",playerSuccessPayload.player().getName());
                    assertEquals("ozdemir",playerSuccessPayload.player().getSurname());
                    assertEquals("PG",playerSuccessPayload.player().getPosition());
                    id.set(playerSuccessPayload.player().getId());
                });

        assertEquals(currentPlayerCount+1,repository.count());
        repository.deleteById(id.get());
    }

    @Test
    @Order(3)
    void shouldFailOnInvalidPosition() {

        String errorMsg = "Invalid Player Position, The valid positions are: {'PG','SG','SF','PF','C'}";
        // language=graphql
        String document = """
            mutation AddPlayer($name: String!, $surname: String!, $position: String!){
                AddPlayer(player: {name: $name, surname : $surname, position: $position }){
                    ... on PlayerFailedPayload{
                        error
                    }
                }
            }
        """;

        graphQlTester.document(document)
                .variable("name","dummyname")
                .variable("surname","dummysurname")
                .variable("position","ZX")
                .execute()
                .path("AddPlayer")
                .entity(PlayerFailedPayload.class)
                .satisfies(playerFailedPayload -> {
                    assertEquals(playerFailedPayload.error(),errorMsg);
                });
    }

    @Test
    @Order(4)
    void shouldFailOnEmptyName(){

        String errorMsg = "Name or surname cannot be empty";

        // language=graphql
        String document = """
            mutation AddPlayer($name: String!, $surname: String!, $position: String!){
                AddPlayer(player: {name: $name, surname : $surname, position: $position }){
                    ... on PlayerFailedPayload{
                        error
                    }
                }
            }
        """;

        graphQlTester.document(document)
                .variable("name","")
                .variable("surname","dummysurname")
                .variable("position","C")
                .execute()
                .path("AddPlayer")
                .entity(PlayerFailedPayload.class)
                .satisfies(playerFailedPayload -> {
                    assertEquals(playerFailedPayload.error(),errorMsg);
                });

    }

    @Test
    @Order(5)
    void shouldFailOnMaximumCapacityReached(){
        fillTheTeam();
        String maximumCapacityReachedErrorMsg = "maximum number of players reached (12)! Please delete players before adding more.";

        //language=graphql
        String document = """
            mutation AddPlayer($name: String!, $surname: String!, $position: String!){
                AddPlayer(player: {name: $name, surname : $surname, position: $position }){
                    ... on PlayerFailedPayload{
                        error
                    }
                }
            }
        """;

        graphQlTester.document(document)
                .variable("name","dogukan")
                .variable("surname","ozdemir")
                .variable("position","PG")
                .execute()
                .path("AddPlayer")
                .entity(PlayerFailedPayload.class)
                .satisfies(playerFailedPayload -> {
                    assertEquals(playerFailedPayload.error(),maximumCapacityReachedErrorMsg);
                });
    }

    @Test
    @Order(6)
    void shouldDeletePlayer(){

        int lastId = (int)repository.getLastId();
        int currentPlayerCount = (int)repository.count();
        // language=graphql
        String document = """
            mutation DeletePlayer($id : ID!){
                DeletePlayer(id : $id){
                    ... on PlayerSuccessPayload{
                        player {
                            id                        
                       }
                    }
                }
            }
        """;

        graphQlTester.document(document)
                .variable("id",lastId)
                .executeAndVerify();

        assertEquals(currentPlayerCount - 1, repository.count());
    }

    @Test
    @Order(7)
    void shouldNotFindPlayerOnDelete(){
        int lastId = (int)repository.getLastId();
        String errorMsg = "player with id " + (lastId+1) + " does not exist!";
        // language=graphql
        String document = """
            mutation DeletePlayer($id : ID!){
                DeletePlayer(id : $id){
                     ...on PlayerFailedPayload{
                       error
                     }
                 }
            }
        """;

        graphQlTester.document(document)
                .variable("id",lastId+1)
                .execute()
                .path("DeletePlayer")
                .entity(PlayerFailedPayload.class)
                .satisfies(playerFailedPayload -> {
                    assertEquals(playerFailedPayload.error(),errorMsg);
                });
    }


    void fillTheTeam(){
        for(int i = 0; i < 12; i++){
            Player p = new Player();
            p.setName("asd");
            p.setSurname("asd");
            p.setPosition("SF");
            repository.save(p);
        }
    }
}