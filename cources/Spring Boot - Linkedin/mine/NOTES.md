# Spring Framework
- framework for providing comprehensive infra support for developing java apps
- provides plumbing and scafooloding
- OOP and DRY and abstractions

# Definitions
- POJO
  - plain old java objsects
- JavaBeans
  - Objects with only getters and setters
- Spring Beans
  - POJOs configured in the application context
- DTO(Data Transfer Objects)
  - Java Beans used to move state between layers of code

# Simple to Use
- Well Documented
- Commen patterns across different uses 
- Config by component scanning via annotations

# Inversion Of Control
- Configuration is building the IoC container
- IoC provides mechanism of dependency injection
- ApplicationContext wraps the beanFactory, which serves the beans to the runtime of the application
- SpringBoot Provides auto- configuration of the Application Context
- Course - "Spring in Depth"

# Spring Boot Benefits
- Supports rapid development
- Removes boilerplate of application setup
- Many modern application uses
- Traditional and cloud native supports
- AUto configuration and embedded offerings

# Key Conmpoenets
- Embedded Servelet Containers
  -  Embedded apache tomcat
- Auto-Configuration of application context
- automatic servlet mappings
- Embedded database suport and hibernate/JPA dialect
- Automatic controller mappings

--

We can focus on the spring and the env is auto configured by the framework

# Annotations for auto configuration and component scanning
- native support in java
- metadata for code
- often used for compiler or runtime instructions
- great leverage point for pointcuts for aspected code(what is aspecting?)

# Configuration
- old = xml configuration
- java configuration 
Beans destined for bean factory which is a central component of the IoC container can be defined with @Bean
- Component Scanning (easiest)
Component scanning is the easiest method of configuration today. Through this method, annotations are added to classes, attributes, and methods that allow the framework to define a Bean for use in the application.
Java configuration is driven by component scanning of the annotated class and then the methods within it. 
- Auto-Configuration
Auto-configuration is an aspect of Spring Boot where Beans are added to the BeanFactory based on annotations and conditions. This is the power of Spring Boot because it allows default Bean creation based on the presence or absence of classes in the class path or already in the BeanFactory.

# Auto-Configuration
- powerful but gracefult set of operations
- @EnableAutoConfiguration
- IoC container configured based on class path
- Can be customized
- Driven by properties
- Developer can configure everything

# Proxies
Outside of configuration and other potential uses, behavior can be added to configured Beans specifically through the proxy pattern. Beans in the BeanFactory are proxied by Spring, Spring adds various types of behavior based on the class that is being proxied and any annotations on it. 

- beans in bean factory are proxied
- annotations drive proxies
- annotations are easy extension points
- order of method calls


> Functional requirements
> Non-functional requirements
> Scale estimation
> Read/write ratios
> Latency vs throughput
> Availability vs consistency
> CAP theorem
> Load balancers
> API gateways
> Caching
> CDN
> Cache invalidation
> TTL
> LRU / LFU
> Write-through / write-back
> SQL vs NoSQL
> Relational vs document vs key-value
> Indexing
> Full-text search
> Sharding
> Partitioning
> Replication
> Leader election
> Quorums
> Consensus
> Distributed locks
> Idempotency
> Exactly-once vs at-least-once
> Queues
> Pub-sub
> Kafka
> RabbitMQ / SQS
> Event-driven architecture
> Backpressure
> Retries
> Dead-letter queues
> Rate limiting
> Token bucket
> Leaky bucket
> WebSockets
> SSE
> Long polling
> Fanout
> Feed generation
> Search ranking
> Object storage
> Blob storage
> Data warehouses
> Batch vs stream processing
> Observability
> Metrics
> Logs
> Traces
> SLOs / SLAs
> Authentication
> Authorization
> Encryption at rest / in transit
> Schema migrations
> Zero-downtime deploys
> Canary releases
> Rollbacks
> Disaster recovery
> RPO / RTO
> Multi-AZ / multi-region
> Cost trade-offs
> Blast radius
> Hot partitions
> Hot keys
> Thundering herd
> Cold starts
> Designing TinyURL
> Designing Dropbox
> Designing Uber
> Designing WhatsApp
> Designing Instagram
> Designing YouTube
> Designing Ticketmaster
> Designing a rate limiter
> Designing a notification system
> Designing a payment system
> Designing a search engine
> Designing a logging pipeline
> Designing a leaderboard
> Designing a live streaming system
> Designing a food delivery system
> Designing a chat app
> Designing a news feed
> How to clarify requirements without sounding lost
> How to stop saying “it depends” every 20 seconds
> How to pick one database and defend it
> How to deep dive without drowning
> How to explain trade-offs while the interviewer keeps saying “scale it more”
> How to recover when your cache makes everything worse
> How to stay calm when your booking system just oversold 400 seats
> How to finish the interview before your diagram becomes modern art
