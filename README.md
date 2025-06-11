
# KubeAuction

A Spring Boot microservice for managing auctions and bids with MongoDB persistence and Redis caching.

## Table of Contents

- [Features](#features)  
- [Tech Stack](#tech-stack)  
- [Getting Started](#getting-started)  
  - [Prerequisites](#prerequisites)  
  - [Configuration](#configuration)  
  - [Running the App](#running-the-app)  
- [API Endpoints](#api-endpoints)  
  - [Users](#users)  
  - [Auctions](#auctions)  
  - [Bids](#bids)  
- [Data Model](#data-model)  
- [Caching Strategy](#caching-strategy)  
- [Aggregation Examples](#aggregation-examples)  
- [Error Handling](#error-handling)  

---

## Features

- CRUD for Users, Auctions, and Bids  
- Soft-delete support (flags instead of physical delete)  
- MongoDB for primary storage  
- Redis for manual caching with TTL  
- Aggregation pipelines for querying bids by user or auction  
- Centralized exception handling via `@RestControllerAdvice`  

---

## Tech Stack

- Java 21 
- Spring Boot  
- MongoDB  
- Redis  

---

## Getting Started

### Prerequisites

### Configuration

## API Endpoints
    
### Users

-- **POST** `/api/users`  
  Create a new user.

- **GET** `/api/users/{id}`  
  Retrieve a user by ID.

- **PUT** `/api/users/{id}`  
  Update user details (soft-delete flag is not modified here).

- **DELETE** `/api/users/{id}`  
  Soft-delete user; also marks their bids and auctions as deleted.

### Auctions

- **POST** `/api/auctions`
  Create a new post

- **GET** `/api/auctions/{id}`
  Retrieve an auction by ID

- **PUT** `/api/auctions/{id}`
  Update an auction by ID like it's done for users.

- **DELETE** `/api/auctions/{id}`
  Deletes an auction by ID (soft delete), also marks all related bids.

### Bids
- **POST** `/api/bids`
  Create a new bids 

- **GET** `/api/bids/{id}`
  Retrieve an bids by ID

- **PUT** `/api/bids/{id}`
  Update an bids by ID like it's done for users.

- **DELETE** `/api/bids/{id}`
  Deletes an bids by ID (soft delete), also marks all related .

## Data Model

### UserEntity
- `ObjectId id`  
- `String name`  
- `String email`  
- `String pwd` (bcrypt-hashed)  
- `Boolean isDeleted`  

### AuctionEntity
- `ObjectId id`  
- `String title`  
- `String descr`  
- `UUID imageId`  
- `ObjectId ownerId`  
- `Date endDate`  
- `Double minimumPrice`  
- `Boolean isDeleted`  
- `Boolean ownerDeleted`  

### BidEntity
- `ObjectId id`  
- `ObjectId auctionId`  
- `ObjectId userId`  
- `int value`  
- `Date createdAt`  
- `Boolean isDeleted`  
- `Boolean userDeleted`  
- `Boolean auctionDeleted`  
## Caching Strategy

All manual Redis caching uses keys in the form:

- Users: `user:{id}`
- Auctions: `auctions:{id}`
- Bids: `bids:{id}`

On reads (e.g. `findById`), the service first attempts to load from Redis; on cache miss, it falls back to MongoDB and then caches the result with a TTL.

On create/update/delete operations, the service writes through to both MongoDB and Redis (or evicts the key on delete) to keep cache and database in sync.

## Testing
