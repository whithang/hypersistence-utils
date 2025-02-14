package io.hypersistence.utils.hibernate.type.json;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.internal.JacksonUtil;
import io.hypersistence.utils.hibernate.util.AbstractPostgreSQLIntegrationTest;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.hibernate.Session;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.TypeDef;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.junit.Test;

import javax.persistence.*;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLJsonNodeTypeTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Book.class
        };
    }

    @Override
    protected void additionalProperties(Properties properties) {
        properties.put(
            EntityManagerFactoryBuilderImpl.METADATA_BUILDER_CONTRIBUTOR,
            (MetadataBuilderContributor) metadataBuilder -> metadataBuilder.applyBasicType(
                JsonNodeBinaryType.INSTANCE
            )
        );
    }

    @Override
    protected void afterInit() {
        doInJPA(entityManager -> {

            Book book = new Book();
            book.setIsbn("978-9730228236");
            book.setProperties(
                JacksonUtil.toJsonNode(
                    "{" +
                        "   \"title\": \"High-Performance Java Persistence\"," +
                        "   \"author\": \"Vlad Mihalcea\"," +
                        "   \"publisher\": \"Amazon\"," +
                        "   \"price\": 44.99" +
                        "}"
                )
            );

            entityManager.persist(book);
        });
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            Book book = session
                .bySimpleNaturalId(Book.class)
                .load("978-9730228236");

            SQLStatementCountValidator.reset();

            book.setProperties(
                JacksonUtil.toJsonNode(
                    "{" +
                        "   \"title\": \"High-Performance Java Persistence\"," +
                        "   \"author\": \"Vlad Mihalcea\"," +
                        "   \"publisher\": \"Amazon\"," +
                        "   \"price\": 44.99," +
                        "   \"url\": \"https://www.amazon.com/High-Performance-Java-Persistence-Vlad-Mihalcea/dp/973022823X/\"" +
                        "}"
                )
            );
        });

        SQLStatementCountValidator.assertTotalCount(1);
        SQLStatementCountValidator.assertUpdateCount(1);
    }

    @Test
    public void testLoad() {
        SQLStatementCountValidator.reset();

        doInJPA(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            Book book = session
                .bySimpleNaturalId(Book.class)
                .load("978-9730228236");

            assertEquals(expectedPrice(), book.getProperties().get("price").asText());
        });

        SQLStatementCountValidator.assertTotalCount(1);
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertUpdateCount(0);
    }

    @Test
    public void testNativeQueryResultTransformer() {
        doInJPA(entityManager -> {
            List<BookDTO> books = entityManager.createNativeQuery(
                "SELECT " +
                "       b.id as id, " +
                "       b.properties as properties " +
                "FROM book b")
            .unwrap(NativeQuery.class)
            .setResultTransformer(new AliasToBeanResultTransformer(BookDTO.class))
            .getResultList();

            assertEquals(1, books.size());
            BookDTO book = books.get(0);

            assertEquals(expectedPrice(), book.getProperties().get("price").asText());
        });
    }

    @Test
    public void testNativeQueryResultMapping() {
        doInJPA(entityManager -> {
            List<BookDTO> books = entityManager.createNativeQuery(
                "SELECT " +
                "       b.id as id, " +
                "       b.properties as properties " +
                "FROM book b")
            .unwrap(NativeQuery.class)
            .setResultSetMapping("BookDTO")
            .getResultList();

            assertEquals(1, books.size());
            BookDTO book = books.get(0);

            assertEquals(expectedPrice(), book.getProperties().get("price").asText());
        });
    }

    protected String initialPrice() {
        return "44.99";
    }

    protected String expectedPrice() {
        return "44.99";
    }

    public static class BookDTO {

        private long id;

        private JsonNode properties;

        public BookDTO() {
        }

        public BookDTO(Number id, JsonNode properties) {
            this.id = id.longValue();
            this.properties = properties;
        }

        public Long getId() {
            return id;
        }

        public void setId(Number id) {
            this.id = id.longValue();
        }

        public JsonNode getProperties() {
            return properties;
        }

        public void setProperties(JsonNode properties) {
            this.properties = properties;
        }
    }

    @Entity(name = "Book")
    @Table(name = "book")
    @TypeDef(typeClass = JsonBinaryType.class, defaultForType = JsonNode.class)
    @SqlResultSetMapping(
        name = "BookDTO",
        classes = {
            @ConstructorResult(
                targetClass = BookDTO.class,
                columns = {
                    @ColumnResult(name = "id"),
                    @ColumnResult(name = "properties", type = JsonNode.class),
                }
            )
        }
    )
    public static class Book {

        @Id
        @GeneratedValue
        private Long id;

        @NaturalId
        private String isbn;

        @Column(columnDefinition = "jsonb")
        private JsonNode properties;

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public JsonNode getProperties() {
            return properties;
        }

        public void setProperties(JsonNode properties) {
            this.properties = properties;
        }
    }
}
