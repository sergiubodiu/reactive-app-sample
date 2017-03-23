# Spring 5 Reactive Web Sample

[Original Sample](https://github.com/poutsma/web-function-sample)

This repository contains a sample application that uses the functional web framework introduced in Spring 5.
It consists of the following types:

| Class                   | Description                                   |
| ----------------------- | --------------------------------------------- |
| `Person`                | Document POJO representing a person           |
| `PersonRepository`      | Reactive repository for `Person` [Mongo]      |
| `PersonHandler`         | Web handler that exposes a `PersonRepository` |
| `ServerApplication`     | Contains a `main` method to start the server  |
| `Client`                | Contains a `main` method to start the client  |

### Running the MongoDB server
 - Install local MongoDB database via `Brew`
```sh
mongod --config /usr/local/etc/mongod.conf
```

### Running the Reactor Netty server
 - Build using maven
 - Run the `io.pivotal.apac.ServerApplication` class
 
### Running the Client
 - Build using maven
 - Run the `io.pivotal.apac.Client` class

### License
This sample released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0

