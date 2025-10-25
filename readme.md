# 🧩 Spring MVC – Configuration complète sans `web.xml`

Ce projet démontre la configuration complète d’une application **Spring MVC avec Spring Data JPA et Spring Core**, entièrement basée sur **Java Config** (sans XML).  
Il met en œuvre les concepts fondamentaux de Spring : **IoC**, **DI**, **JPA**, et **MVC**.

---

## 📘 Sommaire

1. [Spring Core – IoC, Beans et Configuration](#-1-spring-core--ioc-beans-et-configuration)
2. [Spring Data JPA – Persistance et Transactions](#-2-spring-data-jpa--persistance-et-transactions)
3. [Spring MVC – Contrôleurs et DispatcherServlet](#-3-spring-mvc--contrôleurs-et-dispatcherservlet)
4. [Structure du projet](#-structure-du-projet)
5. [Configuration Docker](#-configuration-docker)
6. [Configuration Maven (pom.xml)](#-configuration-maven-pomxml)
7. [Résumé technique](#-résumé-technique)

---

## ⚙️ 1. Spring Core – IoC, Beans et Configuration

Spring Core est le cœur du framework.  
Il gère la création et la configuration des objets via l’**Inversion de Contrôle (IoC)** et l’**Injection de Dépendances (DI)**.

### 🎯 Objectifs
- Centraliser la gestion des dépendances.
- Favoriser un code découplé, modulaire et testable.
- Configurer l’application sans XML grâce à `@Configuration` et `@ComponentScan`.

### 📄 Exemple de configuration : `WebConfig.java`

```java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.example")
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper om = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        converters.add(new MappingJackson2HttpMessageConverter(om));
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new MappingJackson2HttpMessageConverter(mapper);
    }
}
```

---

## 🗄️ 2. Spring Data JPA – Persistance et Transactions

Spring Data JPA simplifie la gestion des entités et l’accès aux données grâce à Hibernate.

### 🎯 Objectifs
- Configurer la base de données et le moteur JPA.
- Gérer les transactions automatiquement.
- Fournir une intégration fluide avec les repositories Spring.

### 📄 Exemple : `PersistenceConfig.java`

```java
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.repository")
public class PersistenceConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://db:5432/user_manager_db");
        ds.setUsername("postgres");
        ds.setPassword("postgres");
        return ds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.example.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        em.setJpaProperties(props);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}
```

---

## 🌐 3. Spring MVC – Contrôleurs et DispatcherServlet

Spring MVC gère la partie web de l’application : les routes, les contrôleurs et les vues (ou JSON).

### 📄 Exemple : `AppInitializer.java`

```java
public class AppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);

        ServletRegistration.Dynamic dispatcher =
                servletContext.addServlet("dispatcher", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
    }
}
```

---

## 🧱 Structure du projet

```
src/
└── main/
├── java/
│ └── com/example/
│ ├── config/
│ │ ├── AppInitializer.java
│ │ ├── PersistenceConfig.java
│ │ └── WebConfig.java
│ ├── controller/
│ │ └── UserController.java
│ ├── dto/
│ │ ├── UserCreateDTO.java
│ │ ├── UserResponseDTO.java
│ │ └── UserUpdateDTO.java
│ ├── entity/
│ │ ├── Role.java
│ │ └── User.java
│ ├── exception/
│ │ ├── EmailAlreadyExistsException.java
│ │ └── NotFoundException.java
│ ├── mapper/
│ │ └── UserMapper.java
│ ├── repository/
│ │ └── UserRepository.java
│ ├── service/
│ │ └── UserService.java
│ └── web/advice/
│ └── GlobalExceptionHandler.java
└── resources/
└── application.properties

Autres fichiers :
├── docker-compose.yml
├── pom.xml
├── .gitignore
└── spring-mvc-crud.iml

```

---

## 🐳 Configuration Docker

```yaml
services:
  db:
    image: postgres:latest
    container_name: management_users_postgres
    environment:
      POSTGRES_DB: user_manager_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "1111:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U Usermanager -d user_manager_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  pgadmin:
    image: dpage/pgadmin4
    container_name: user_management_pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - db

  tomcat:
    image: tomcat:9.0-jdk17
    container_name: user_management_tomcat
    ports:
      - "8086:8080"
    depends_on:
      db:
        condition: service_healthy
    volumes:
    - ./target/spring-user-mgt.war:/usr/local/tomcat/webapps/ROOT.war

    command: [ "catalina.sh", "run" ]

volumes:
  postgres_data:
```

---

## 🧩 Configuration Maven (pom.xml)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.spring</groupId>
    <artifactId>user_management</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring.version>5.3.31</spring.version>
        <hibernate.version>5.6.15.Final</hibernate.version>
    </properties>

    <dependencies>
        <!-- SPRING CORE -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <!-- SPRING MVC -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <!-- SPRING DATA JPA -->
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-jpa</artifactId>
            <version>2.7.14</version>
        </dependency>
        <!-- HIBERNATE -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>
        <!-- JPA API -->
        <dependency>
            <groupId>javax.persistence</groupId>
            <artifactId>javax.persistence-api</artifactId>
            <version>2.2</version>
        </dependency>
        <!-- VALIDATION API -->
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <version>2.0.1.Final</version>
        </dependency>
        <!-- HIBERNATE VALIDATOR -->
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>6.2.5.Final</version>
        </dependency>
        <!-- JACKSON (JSON) -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
        <!-- SERVLET API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
        <!-- H2 DATABASE (DEV/TEST) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.3</version>
        </dependency>
        <!-- JUNIT (TESTS) -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.9</version>
        </dependency>

        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.4.14</version>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.15.2</version>
        </dependency>

    </dependencies>

    <build>
        <finalName>spring-user-mgt</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 📋 Résumé technique

| Composant | Classe principale | Description |
|------------|------------------|--------------|
| **Spring Core** | `WebConfig` | Gère les beans, l’injection et la configuration globale |
| **Spring Data JPA** | `PersistenceConfig` | Configure la base de données, JPA et les transactions |
| **Spring MVC** | `AppInitializer`, `WebConfig` | Configure le DispatcherServlet et la sérialisation JSON |
| **Base de données** | PostgreSQL | Connectée via `DriverManagerDataSource` |
| **Docker** | `docker-compose.yml` | Contient PostgreSQL, pgAdmin et Tomcat |
| **Build** | `pom.xml` | Gère les dépendances et la création du WAR |

---

## 🚀 Conclusion

Ce projet illustre une configuration **100 % Java**, sans fichier XML, en exploitant la puissance de **Spring Core**, **Spring Data JPA** et **Spring MVC**.

### ✅ Avantages :
- Code plus lisible et modulaire.
- Maintenance simplifiée.
- Meilleure intégration entre les couches (Web, Service, Data).
- Facilement extensible pour une architecture REST complète.

---
