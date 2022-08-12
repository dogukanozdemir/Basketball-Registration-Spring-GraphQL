# Project Overview

This a spring Boot application that can be used for Basketball team registrations.
A user can add a player, delete a player or display all of the players in the system.
The project offers the newest approach for testing Spring GraphQL applications using `spring-graphql-test` and `GraphQlTester`
Specifically, Integration testing with JUnit5.

# Table of Contents
1. [How does GraphQL](#graphql)
2. [GraphQL Schema](#schema)
3. [Types](#types)
4. [Exceptions](#exceptions)
5. [Installation](#installation)
6. [License](#license)


# How does GraphQL work? <a name="graphql"></a>

![GraphQL](https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQF8LEvP0aLNOz_5C7Qc0AQPXYLfENHxxzzQ2p8rfZjsqmRtRO548OuLdlskrA6VgHB0A&usqp=CAU)

Traditional REST APIs work with the concept of Resources that the server manages. We can manipulate these resources in some standard ways, following the various HTTP verbs. This works very well as long as our API fits the resource concept, but quickly falls apart when we need to deviate from it.

This also suffers when the client needs data from multiple resources at the same time, such as requesting a blog post and the comments. Typically, this is solved either by having the client make multiple requests, or having the server supply extra data that might not always be required, leading to larger response sizes.

GraphQL offers a solution to both of these problems. It allows the client to specify exactly what data it desires, including from navigating child resources in a single request, and allows for multiple queries in a single request.

### GraphQL Schema <a name="schema"></a>

```graphql

mutation {
  DeletePlayer(id :4){
    ...on PlayerSuccessPayload{
      message
      player{
        id
        name
        surname
        position
      }
    }
    ...on PlayerFailedPayload{
      error
    }
  }
  
  AddPlayer(player: {name : "dogukan", surname : "ozdemir", position : "PG"}){
    ...on PlayerSuccessPayload{
      message
      player {
        id
        name
        surname
        position
      }
    }
    ...on PlayerFailedPayload {
      error
    }
  }
}

query {
	getAllPlayers{
    id
    name
    surname
    position
  }
}

```

### Types <a name="types"></a>

`union PlayerPayload = PlayerSuccessPayload | PlayerFailedPayload`

Every mutation returns a `PlayerPayload` type, which is either a `PlayerSuccessPlayload` or `PlayerFailedPayload`.

```graphql

type PlayerFailedPayload {
    error: String!
}

type PlayerSuccessPayload {
    message: String!
    player : Player!
}

```

### Payload Exceptions <a name="exceptions"></a>

- **Maximum number of players reached**:
  
  Assume the system has 12 players already registered, if a client sends request to `AddPlayer` the mutation would return `PlayerFailedPayload`
  
  *Example:*
  
  Mutation:
  ```graphql
  
  AddPlayer(player: {name : "dogukan", surname : "ozdemir", position : "PG"}){
    ...on PlayerFailedPayload {
      error
    }
  }
  
  ```
  
  The response should look like:
  ```graphql
  
  {
  "data": {
    "AddPlayer": {
      "error": "maximum number of players reached (12)! Please delete players before adding more."
      }
    }
  }
  
  ```
- **Empty name or surname exception**:
  
  an empty parameter in name or surname for `AddPlayer`
  *Example:*
  
  Mutation:
  ```graphql
  
  AddPlayer(player: {name : "", surname : "asd", position : "PG"}){
    ...on PlayerFailedPayload {
      error
    }
  }
  
  ```
  
  The response should look like:
  ```graphql
  
    {
      "data": {
        "AddPlayer": {
          "error": "Name or surname cannot be empty"
        }
      }
    }
  
  ```
- **Invalid Player position exception**
  
  an Invalid player position for parameter "position" in `AddPlayer`.
  *Example:*
  
  Mutation:
  ```graphql
  
  AddPlayer(player: {name : "ahmet", surname : "bulut", position : "Z"}){
    ...on PlayerFailedPayload {
      error
    }
  }
  
  ```
  
  The response should look like:
  ```graphql
  
  {
    "data": {
      "AddPlayer": {
        "error": "Invalid Player Position, The valid positions are: {'PG','SG','SF','PF','C'}"
      }
    }
  }
  
  ```
- Invalid id for DeletePlayer exception:
  
  an Invalid id  for parameter "id" in `DeletePlayer`.
  *Example:*
  
  Mutation:
  ```graphql
  
  DeletePlayer(id : 3){
    ...on PlayerFailedPayload {
      error
    }
  }
  
  ```
  
  The response should look like:
  ```graphql
  
  {
    "data": {
      "DeletePlayer": {
        "error": "player with id 3 does not exist!"
      }
    }
  }
  
  ```
  
### Installation <a name="installation"></a>
  
  Clone the repository to your directory of your choice.
  For users of Intellij IDE:
  
  `File -> Open -> /path/to/your/directory` 
  
  To run the application, run `BasketballApplication.java` and the program should run on `localhost:8080/graphiql`
  To run the Integration test, run `PlayerControllerIntTest.java` and the program should test all of the endpoints and edge cases.
  

### License <a name="license"></a>

 This project is licensed under the terms of the MIT license.
