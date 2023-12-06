package dev.danvega.jdbcblog.post;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.LocalDate;

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
