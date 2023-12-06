package dev.danvega.jdbcblog.post;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface PostRepository extends ListCrudRepository<Post,Integer> {

    Optional<Post> findBySlug(String slug);

}
