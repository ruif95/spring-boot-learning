package com.example.spring_boot_learning.content.repository;

import org.springframework.stereotype.Repository;

@Repository
public class ContentJdbcTemplateRepository {

    /*private final JdbcTemplate jdbcTemplate;

    public ContentJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static Content mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Content(rs.getInt("id"),
                rs.getString("title"),
                rs.getString("desc"),
                rs.getString("status"),
                rs.getString("content_type"),
                rs.getTimestamp("date_created"),
                rs.getTimestamp("date_updated"),
                rs.getString("url"));
    }

    public List<Content> getAllContent() {
        String sql = "SELECT * FROM Content";
        List<Content> contents = jdbcTemplate.query(sql, ContentJdbcTemplateRepository::mapRow);
        return contents;
    }

    public Optional<Content> findById(Integer id) {
        String sql = "SELECT * FROM Content WHERE id = ?";
        Content content = jdbcTemplate.queryForObject(sql, new Object[]{ id }, new int[] { Types.INTEGER }, ContentJdbcTemplateRepository::mapRow);
        return Optional.ofNullable(content);
    }

    public void save(Content content) {
        String sql = "UPDATE Content SET title=?, desc=?, status=?, content_type=?, date_updated=NOW(), url=? WHERE id=?";

        jdbcTemplate.update(sql, content.title(), content.desc(), content.status(), content.contentType(), content.url(), content.id());
    }

    public boolean existsById(Integer id) {
        return contentList.stream().anyMatch(c -> c.id().equals(id));
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Content WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }*/
}
