# **KubeAuction** - Auction System with Docker & Kubernetes Deployment

## **Project Overview**

This project is an auction system backend (like eBay) 🛍, now focusing on deploying it using **Docker**  and **Kubernetes** . The system allows users to create auctions, place bids, ask questions, and manage media (images/videos) . In this version, the backend application is containerized and deployed on **Azure Kubernetes Service (AKS)** , using **Redis** for caching and **Persistent Volumes** for media storage .

---

## **Features**

###  **1. Core Functionality**
- Users can create and manage auctions .
- Users can place bids on open auctions 🏷.
- Users can ask questions about auction items, and auction creators can reply .
- Supports media uploads (images/videos) for auctions and user profiles .
- RESTful API built using **Java** with **Jakarta EE** to manage:
  - Users (`/rest/user`)
  - Auctions (`/rest/auction`)
  - Bids (`/rest/auction/{id}/bid`)
  - Questions (`/rest/auction/{id}/question`)
  - Media (`/rest/media`)

###  **2. Deployment Features**
- **Application and Redis deployment in Kubernetes** : The backend server and Redis caching service are deployed using Docker containers in **Azure Kubernetes Service (AKS)**.
- **Persistent Volume for Media Storage** : Media files are stored in a persistent volume managed by Kubernetes.
- **Artillery Testing** : The system's performance is tested under load using **Artillery**.
- **MongoDB**  is used for storing structured data like users, auctions, bids, and questions.

---

## **Technologies Used**

### **Backend Technologies**
- **Java** with **Jakarta EE** for the backend application.
- **MongoDB**  for storing auction and user data.
- **Redis** for caching frequently accessed data.
- **Azure Kubernetes Service (AKS)**  for container orchestration.
- **Docker**  for containerizing the application and Redis.
- **Persistent Volume (Kubernetes)** for media storage.

### **Testing Tools**

- **Artillery** : Artillery is a modern, powerful, and easy-to-use testing toolkit for HTTP, WebSocket, and Socket.io applications. It allows you to simulate real user behavior and measure the performance of your application under load.

  - **Key Features**:
    - **Load Testing**: Artillery can simulate thousands of virtual users, helping you evaluate how your application performs under heavy load. This is crucial for ensuring scalability and reliability.
    - **Scenario Definition**: You can define complex user scenarios in simple YAML format, including multiple requests and response validations, making it easy to model user workflows.
    - **Metrics and Reporting**: Artillery provides detailed reports on response times, error rates, and throughput, allowing you to identify bottlenecks and optimize your application effectively.
    - **Integration with CI/CD**: Artillery can be easily integrated into your continuous integration and deployment pipelines, enabling automated performance testing as part of your development workflow.

  - **How to Use**:
    1. Create a test configuration file (e.g., `test-images.yml`) where you define your test scenarios, including the API endpoints to test, the number of virtual users, and the duration of the test.
    2. Run the tests using the command: `artillery run test-images.yml`. This will initiate the testing process and generate a report once the tests are complete.
    3. Analyze the results to understand how well your application performs and where improvements may be needed.

---

## **System Architecture**

###  **Containers**
1. **Backend Application Container** 🛠:
   - Handles API requests for managing auctions, users, bids, and media.
   - Exposes RESTful API endpoints using **Java/Jakarta EE** ☕️.
2. **Redis Cache Container** :
   - Manages the cache to improve performance, storing frequently accessed data.
3. **MongoDB Container** :
   - Used as the primary database to store structured data for users, auctions, and bids.
4. **Persistent Volume** :
   - Media files (images/videos) are stored on a persistent volume in the Kubernetes cluster.

### **Data Structures**

**User**

```json
{
    "id": "1",
    "name": "José",
    "nickname": "Zé",
    "pwd": "eusouoze",
    "photoId": "39363F1A472827A12BAF9C483E9741607115F243"
}
```

**Auction**

```json
  {
    "id": "1",
    "title": "Seat ibiza",
    "description": "Seat ibiza de 98",
    "imageId": "39363F1A472827A12BAF9C483E9741607115F243",
    "ownerId": "1",
    "endDate": "2022-10-25T18:25:43.511Z",
    "minimumPrice": "15.5",
    "status": "1",
    "winnerId": "null"
}
```

**Bid**

```json
 {
    "id": "1",
    "auctionId": "1",
    "value": "50",
    "userId": "1"
}
```

**Login**

```json
{
    "userId": "1",
    "pwd": "eusouoze"
}
```


---
##  **Deployment**

**1. Build the Application**
To compile your application and create a JAR file, run the following command:

```bash
mvn clean compile package
```

**2. Build Docker Images**
Build your Docker images for the application and the artillery testing service:

```bash
docker build -t yourusername/your-app-name .
docker build -t yourusername/artillery-testing .
```

**3. Push Docker Images to Repository**
Push the built images to your Docker repository:

```bash
docker push yourusername/your-app-name
docker push yourusername/artillery-testing
```

**4. Create Azure Resources**
Use the Java management class to create necessary Azure resources:

```bash
java -cp target/your-app-name-1.0-jar-with-dependencies.jar your.package.AzureManagement
```

**5. Deploy to Azure Container Instances**
Create an Azure container for your application:

```bash
az container create --resource-group your-resource-group --name your-app-container --image yourusername/your-app-name --ports 8080 --dns-name-label your-dns-label --environment-variables STORAGE_CONNECTION_STRING=YourConnectionString REDIS_KEY=YourRedisKey DB_KEY=YourDbKey
```

Create a container for artillery testing:

```bash
az container create --resource-group your-resource-group --name artillery-testing-container --image yourusername/artillery-testing --dns-name-label artillery-dns-label
```

**6. Deploy to Azure Kubernetes Service (AKS)**
Create your AKS cluster:

```bash
az aks create --resource-group your-resource-group --name your-aks-cluster --node-vm-size Standard_B2s --generate-ssh-keys --node-count 2 --service-principal YourServicePrincipal --client-secret YourClientSecret
```

Get credentials for your AKS cluster:

```bash
az aks get-credentials --resource-group your-resource-group --name your-aks-cluster
```

**7. Apply Kubernetes Configurations**
Deploy your services using the appropriate YAML files:

```bash
kubectl apply -f redis.yaml
kubectl apply -f volume-claim.yaml
kubectl apply -f mongo.yaml
kubectl apply -f mongo-express.yaml
kubectl apply -f your-app-name.yaml
```

**8. Verify Deployment**
Check the status of your deployments:

```bash
kubectl get pods
kubectl get services
```

**9. Run Artillery Tests**
To run performance tests, use the following command:

```bash
artillery run test-images.yml
```

**10. Clean Up**
To remove all deployments, services, and pods from Kubernetes:

```bash
kubectl delete deployments,services,pods,pv,pvc --all
```

**11. Cleanup Docker Resources**
If you need to clean up unused Docker resources, you can run:

```bash
docker system prune -a -f
```

### **Note**
This project serves as a practical exercise to help me play around with cloud services, Docker, and Kubernetes.
