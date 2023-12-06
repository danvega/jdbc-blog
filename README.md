# JDBC Client vs Spring Data JDBC

I was asked by a subscriber on my YouTube channel "What is the difference between the new JDBC Client in Spring Boot
3.2 and Spring Data JDBC"? I thought that was a great question and decided to make a video addressing that question. In 
this tutorial we explore the various ways we can communicate with a database in Java & Spring. 

## Getting Started 

To get started you can head over to start.spring.io and create the following project:

- Maven
- Spring Boot 3.2
- Java 21 
- Dependencies 
  - Web
  - JDBC
  - PostgreSQL
  - Docker Compose 

Or you can click [this link](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.2.0&packaging=jar&jvmVersion=21&groupId=dev.danvega&artifactId=jdbc-blog&name=jdbc-blog&description=Demo%20project%20for%20Spring%20Boot&packageName=dev.danvega.jdbc-blog&dependencies=web,jdbc,postgresql,docker-compose) 
which will open that project for you. 


## Post Database

In this tutorial we are creating a simple application modeled around the idea of blog posts.

Create a new file called `schema.sql` and use the following to create a table and insert a single row.

```sql
DROP TABLE IF EXISTS Post; -- only doing this for demo purposes and to clean everything on restart

CREATE TABLE Post (
  id varchar(255) NOT NULL,
  title varchar(255) NOT NULL,
  slug varchar(255) NOT NULL,
  date date NOT NULL,
  time_to_read int NOT NULL,
  tags varchar(255),
  version INT,
  PRIMARY KEY (id)
);

INSERT INTO Post
(id, title, slug, date, time_to_read, tags, version)
VALUES (1,'Hello, World!','hello-world',CURRENT_DATE, 5, 'Spring Boot, Java', null);
```

In `application.properties` set the following property 

```properties
spring.sql.init.mode=always
```

Run the application, test the connection to the database, and verify that 1 record was inserted.

## Data Access in Java 

When it comes to connecting to datasources and accessing data using the JDK there are 2 packages that you will want to look into: 

- [Package `javax.sql`](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/javax/sql/package-summary.html)
- [Package `java.sql`](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/package-summary.html)

In this first example we read all of the posts using the JDK APIs. 

```java
@Service
public class JdbcService {

    private final DataSource dataSource;

    public JdbcService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public List<Post> findAll() throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement("select * from Post");
        ResultSet rs = preparedStatement.executeQuery();

        List<Post> posts = new ArrayList<>();
        while (rs.next()) {
            posts.add(new Post(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("slug"),
                    rs.getDate("date").toLocalDate(),
                    rs.getInt("time_to_read"),
                    rs.getString("tags"),
                    rs.getInt("version")));
        }

        return posts;
    }
}
```

## JDBC Template

The [`JdbcTemplate`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html) 
can be used directly for many data access purposes, supporting any kind of JDBC Operation. This class 
simplifies the use of JDBC and helps to avoid common errors. It executes core JDBC workflow, leaving application code to 
provide SQL and extract results.


Create a `Post` record that will represent a single post in our application: 

```java
public record Post(
        String id,
        String title,
        String slug,
        LocalDate date,
        int timeToRead,
        String tags
) {

}
```

```java
@Service
public class PostService {

    private final JdbcTemplate jdbcTemplate;

    public PostService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Post> findAll() {
        return jdbcTemplate.query("select * from Post",(rs,rowNum) -> new Post(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getDate("date").toLocalDate(),
                rs.getInt("time_to_read"),
                rs.getString("tags")
        ));
    }
}

```


## JDBC Client 

The [`JdbcClient`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/simple/JdbcClient.html) 
is a A fluent JdbcClient with common JDBC query and update operations, supporting JDBC-style positional as well as Spring-style 
named parameters with a convenient unified facade for JDBC PreparedStatement execution.


```java
@Service
public class PostService {

    private final JdbcClient jdbcClient;

    public PostService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Post> findAll() {
        return jdbcClient.sql("SELECT id,title,slug,date,time_to_read,tags FROM post")
                .query(Post.class)
                .list();
    }

    public Optional<Post> findById(String id) {
        return jdbcClient.sql("SELECT id,title,slug,date,time_to_read,tags FROM post WHERE id = :id")
                .param("id", id)
                .query(Post.class)
                .optional();
    }

    public int create(Post post) {
        return jdbcClient.sql("INSERT INTO post(id,title,slug,date,time_to_read,tags) values(?,?,?,?,?,?)")
                .params(List.of(post.id(),post.title(),post.slug(),post.date(),post.timeToRead(),post.tags()))
                .update();
    }

    public int update(Post post, String id) {
        return jdbcClient.sql("update post set title = ?, slug = ?, date = ?, time_to_read = ?, tags = ? where id = ?")
                .params(List.of(post.title(), post.slug(), post.date(), post.timeToRead(), post.tags(), id))
                .update();
    }

    public int delete(String id) {
        return jdbcClient.sql("delete from post where id = :id")
                .param("id", id)
                .update();
    }

}
```


## Spring Data

[Spring Data’s](https://spring.io/projects/spring-data) mission is to provide a familiar and consistent, Spring-based programming 
model for data access while still retaining the special traits of the underlying data store.

First you will need to update the `Post` Record with the appropriate Spring Data annotations: 

```java
public record Post(
        @Id
        String id,
        String title,
        String slug,
        LocalDate date,
        int timeToRead,
        String tags,
        @Version
        Integer version
) {

}
```

Create `PostRepository.java` which will give us all of the CRUD functionality we need out of the box. You can also 
use Query Derivation to define your own custom queries. 

```java
public interface PostRepository extends ListCrudRepository<Post,Integer> {

    Optional<Post> findBySlug(String slug);

}
```

To complete the Spring Data example you can update the `CommandLineRunner`:

```java
@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(PostRepository repository) {
        return args -> {
            repository.save(new Post("1234","Hello, World!","hello-world", LocalDate.now(),10,"Spring Boot",null));
            List<Post> posts = repository.findAll();
            System.out.println(posts);


            Optional<Post> hello = repository.findBySlug("hello-world");
            System.out.println(hello);
        };
    }

}
```