package dev.danvega.jdbcblog;

import dev.danvega.jdbcblog.post.Post;
import dev.danvega.jdbcblog.post.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
