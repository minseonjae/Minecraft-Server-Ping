# Minecraft-Server-Ping

## Features

- Simple pinging of Minecraft Java Edition servers
- Automatically resolves SRV records
- Retrieves server version, player count, MOTD, and ping time
- Option to parse response in simplified or full format
- Fully implemented using raw sockets (no external server required)

## Installation

Add the following dependencies to your `build.gradle` or `pom.xml`:

```groovy
dependencies {
    implementation 'dnsjava:dnsjava:3.6.2'
    implementation 'com.google.code.gson:gson:2.11.0'
    implementation 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'
}
```

## Usage

### Simple usage:

```java
MinecraftServerSimpleResponse response = MinecraftServerPing.getServerPing("mc.hypixel.net");
System.out.println("Players Online: " + response.getOnlinePlayers());
```

### Full usage (port, timeout, protocol version):

```java
MinecraftServerSimpleResponse response = MinecraftServerPing.getServerPing("example.com", 25565, 1500, 762, false);
```

## Method Overloads

The class `MinecraftServerPing` provides several overloaded `getServerPing` methods:

- `getServerPing(String hostname)`
- `getServerPing(String hostname, boolean simple)`
- `getServerPing(String hostname, int port)`
- `getServerPing(String hostname, int port, boolean simple)`
- `getServerPing(String hostname, int port, int timeout)`
- `getServerPing(String hostname, int port, int timeout, boolean simple)`
- `getServerPing(String hostname, int port, int timeout, int protocolVersion)`
- `getServerPing(String hostname, int port, int timeout, int protocolVersion, boolean simple)`

## Returned Data

The returned `MinecraftServerSimpleResponse` (or `MinecraftServerResponse`) includes:

- `protocolName` — The protocol version name
- `protocol` — The protocol version number
- `maxPlayers` — Maximum number of players allowed
- `onlinePlayers` — Current number of online players
- `motd` — JSON element representing the server's MOTD (Message of the Day)
- `status_ping` — Ping for status packet
- `connect_ping` — Ping for TCP connection
- `favicon` *(only if full response is enabled)*

## License

This project is licensed under the MIT License.
You are free to use, modify, and distribute this library for any purpose, including commercial use, as long as the original copyright notice is included.

---

Developed with️ by [CodingTree.kr](https://codingtree.kr)
