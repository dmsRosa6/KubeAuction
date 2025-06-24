
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

### Module Structure

```
kubeauction/
├── kubeauction-api/          # REST API endpoints
├── kubeauction-shared/       # Shared domain models & utilities
├── auction-worker/           # Background processing service
├── kubernetes/              # K8s deployment manifests
└── docker-compose.yml       # Local development setup

```

#### kubeauction-api

REST API Layer

-   Controllers for users, auctions, and bids
-   Caching logic for read operations
-   Write-through and eviction logic for updates/deletes
-   Aggregation pipelines for complex queries
-   Global exception handling with @RestControllerAdvice

#### kubeauction-shared

Shared Library Module

-   Domain models (UserEntity, AuctionEntity, BidEntity)
-   MongoDB repositories
-   Common Redis utilities
-   DTOs and mappers

#### auction-worker

Background Service

-   Periodic execution using Spring's @Scheduled
-   Auction expiration management
-   Winner determination logic
-   Redis cleanup operations

Data Model
----------

Entities are stored in MongoDB with soft deletion support and reference-based relationships.

### User Entity

```
ObjectId id;
String name;
String email;
String pwd;           // bcrypt-hashed
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

The system uses Redis manually (not via Spring Cache abstraction) for optimized performance:

### Key Patterns

-   user:{id} - User data
-   auctions:{id} - Auction details
-   bids:{id} - Bid information

### Cache Operations

-   Reads: Redis → MongoDB fallback → Cache with TTL
-   Writes: Update MongoDB → Evict/update Redis key
-   Expiration: Redis Sorted Sets track auction end times

Auction Expiration and Notification Workflow
---------------------------

1.  Registration: Auctions added to Redis ZSet with end date as score
2.  Monitoring: Scheduled job queries expired auctions (score ≤ now)
3.  Processing: For each expired auction:
    -   Find highest bid
    -   Execute business logic
    -   Clean up Redis state

Quick Start
-----------

### Prerequisites

-   Java 21+
-   Docker & Docker Compose
-   Maven 3.8+

# Build and start all services

### Docker Deployment

```bash
$ cd KubeAuction
$ ./run.sh
```

### Kubernetes Deployment


Testing (Missing)
-------

The project includes comprehensive test coverage:

-   Unit Tests: JUnit 5 + Mockito for service layer testing
-   Integration Tests: @SpringBootTest + MockMvc for API verification

```
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

```

Technology Stack
----------------

| Purpose | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Database | MongoDB |
| Cache | Redis |
| Scheduling | Spring @Scheduled |
| Testing | JUnit 5, Mockito, MockMvc |
| Containerization | Docker |
| Orchestration | Kubernetes (Kind) |

API Endpoints
-------------

### Users

-   GET /api/users - List all users
-   POST /api/users - Create user
-   GET /api/users/{id} - Get user by ID
-   PUT /api/users/{id} - Update user
-   DELETE /api/users/{id} - Soft delete user

### Auctions

-   GET /api/auctions - List all auctions
-   POST /api/auctions - Create auction
-   GET /api/auctions/{id} - Get auction details
-   PUT /api/auctions/{id} - Update auction
-   DELETE /api/auctions/{id} - Soft delete auction

### Bids

-   GET /api/bids - List all bids
-   POST /api/bids - Place bid
-   GET /api/bids/auction/{auctionId} - Get bids for auction
-   GET /api/bids/user/{userId} - Get user's bids

Current Limitations / Improvements
-------------------

-   Security: No authentication/authorization implemented
-   Notifications: Email/winner notification logic is stubbed
-   Frontend: API-first architecture (no UI)
-   Environment: Designed for local development, not production-ready
-   There is A LOT of room from performance improvements, things like using the cache better, sending the updates in batches to not clog the network more, etc.



Note: This is an educational project focused on learning backend development patterns and technologies. It demonstrates various concepts but is not intended for production use.
