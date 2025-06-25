
# KubeAuction

A simple backend for a bidding platform. This is mainly a study project to understand a little better both Spring and Kubernetes.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=spring) ![MongoDB](https://img.shields.io/badge/MongoDB-4.4+-green?style=flat-square&logo=mongodb) ![Redis](https://img.shields.io/badge/Redis-7.x-red?style=flat-square&logo=redis) ![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=flat-square&logo=docker) ![Kubernetes](https://img.shields.io/badge/Kubernetes-Kind-326ce5?style=flat-square&logo=kubernetes)

KubeAuction is a modular backend application developed as a personal learning project, aimed at exploring and applying real-world backend engineering principles. The project simulates an online auction system with support for managing users, auctions, and bids.

Learning Objectives
-------------------

This project serves as a comprehensive sandbox for experimenting with:

-   API Design: Building RESTful APIs using Spring Boot
-   Modular Architecture: Designing and organizing a maintainable codebase
-   Data Strategy: Integrating MongoDB and Redis in a cohesive approach
-   Caching: Implementing manual caching and cache invalidation logic
-   Background Processing: Running scheduled workers for asynchronous business logic
-   Containerization: Setting up local environments with Docker and Kubernetes

Architecture Overview
---------------------

The system emphasizes clean architecture, soft deletion strategies, caching with Redis, background processing with scheduled jobs, and deployment with Docker and Kubernetes. It's structured into separate modules to improve maintainability and simulate a microservice-like architecture.


Technology Stack
----------------

- Java 21
- Spring Boot 3.x
- MongoDB 4.4+
- Redis 7.x
- Docker & Docker Compose
- Kubernetes (Kind)

### Prerequisites

-   Java 21+
-   Docker & Docker Compose
-   Maven 3.8+


## Data Model


Entities are stored in MongoDB with soft deletion support and reference-based relationships.

### User Entity

```
ObjectId id;
String name;
String email;
String pwd;           
Boolean isDeleted;

```

### Auction Entity

```
ObjectId id;
String title;
String descr;
UUID imageId;
ObjectId ownerId;
Date endDate;
Double minimumPrice;
Boolean isDeleted;
Boolean ownerDeleted;

```

### Bid Entity

```
ObjectId id;
ObjectId auctionId;
ObjectId userId;
int value;
Date createdAt;
Boolean isDeleted;
Boolean userDeleted;
Boolean auctionDeleted;

```

Caching Strategy
----------------

The system uses Redis manually (not via Spring Cache abstraction):

### Key Patterns

-   user:{id} - User data
-   auctions:{id} - Auction details
-   bids:{id} - Bid information

### Cache Operations

-   Reads: Redis → MongoDB fallback → Cache with TTL
-   Writes: Update MongoDB → Evict/update Redis key
-   Expiration: Redis Sorted Sets track auction end times

## Notifications Workflow


1.  Registration: Auctions added to Redis ZSet with end date as score
2.  Monitoring: Scheduled job queries expired auctions (score ≤ now)
3.  Processing: For each expired auction:
    -   Find highest bid
    -   Execute business logic
    -   Clean up Redis state


# Build

### Docker Deployment

```bash
$ cd KubeAuction
$ ./run_docker.sh
```

### Kubernetes Deployment

```bash
$ cd KubeAuction
$ ./run_kubernetes.sh
```

The Api can thhen be reached on localhost:8080 

API Endpoints
-------------

### Users

-   POST /api/users - Create user
-   GET /api/users/{id} - Get user by ID
-   PUT /api/users/{id} - Update user
-   DELETE /api/users/{id} - Soft delete user

### Auctions

-   POST /api/auctions - Create auction
-   GET /api/auctions/{id} - Get auction details
-   PUT /api/auctions/{id} - Update auction
-   DELETE /api/auctions/{id} - Soft delete auction

### Bids

-   POST /api/bids - Place bid
-   GET /api/bids/auction/{auctionId} - Get bids for auction
-   GET /api/bids/user/{userId} - Get user's bids

Current Limitations / Improvements
-------------------

-   Security: No authentication/authorization implemented
-   Notifications: Email/winner notification logic is stubbed using redis pub/sub
-   Frontend: API-first architecture (no UI)
-   There is room for performance improvements related to the cache and db: sending the updates in batches to not clog the network more, using async, redis functions could probably help on a case or other, etc.
-   Things like pagination could be a plus
-   Probabily could test how everything works under load using artillery (its half configured already), and i ditched unit and integration tests that needed to be entirely re done since the last project big refactor 



Note: This is an educational project focused on learning backend development patterns and technologies. It demonstrates various concepts but is not intended for production use.
