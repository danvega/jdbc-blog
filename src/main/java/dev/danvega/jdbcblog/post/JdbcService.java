package dev.danvega.jdbcblog.post;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
