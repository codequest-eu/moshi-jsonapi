package moe.banana.jsonapi2;

import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import moe.banana.jsonapi2.model.Article;
import moe.banana.jsonapi2.model.Comment;
import moe.banana.jsonapi2.model.Person;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("ALL")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DocumentUnitTest {

    private static final String JSON_DATA_1 = "{" +
            "  \"links\": {" +
            "    \"self\": \"http://example.com/articles\"," +
            "    \"next\": \"http://example.com/articles?page[offset]=2\"," +
            "    \"last\": \"http://example.com/articles?page[offset]=10\"" +
            "  }," +
            "  \"data\": [{" +
            "    \"type\": \"articles\"," +
            "    \"id\": \"1\"," +
            "    \"attributes\": {" +
            "      \"title\": \"JSON API paints my bikeshed!\"" +
            "    }," +
            "    \"relationships\": {" +
            "      \"author\": {" +
            "        \"links\": {" +
            "          \"self\": \"http://example.com/articles/1/relationships/author\"," +
            "          \"related\": \"http://example.com/articles/1/author\"" +
            "        }," +
            "        \"data\": { \"type\": \"people\", \"id\": \"9\" }" +
            "      }," +
            "      \"comments\": {" +
            "        \"links\": {" +
            "          \"self\": \"http://example.com/articles/1/relationships/comments\"," +
            "          \"related\": \"http://example.com/articles/1/comments\"" +
            "        }," +
            "        \"data\": [" +
            "          { \"type\": \"comments\", \"id\": \"5\" }," +
            "          { \"type\": \"comments\", \"id\": \"12\" }" +
            "        ]" +
            "      }" +
            "    }," +
            "    \"links\": {" +
            "      \"self\": \"http://example.com/articles/1\"" +
            "    }" +
            "  }]," +
            "  \"included\": [{" +
            "    \"type\": \"people\"," +
            "    \"id\": \"9\"," +
            "    \"attributes\": {" +
            "      \"first-name\": \"Dan\"," +
            "      \"last-name\": \"Gebhardt\"," +
            "      \"twitter\": \"dgeb\"," +
            "      \"age\": 20" +
            "    }," +
            "    \"links\": {" +
            "      \"self\": \"http://example.com/people/9\"" +
            "    }" +
            "  }, {" +
            "    \"type\": \"comments\"," +
            "    \"id\": \"5\"," +
            "    \"attributes\": {" +
            "      \"body\": \"First!\"" +
            "    }," +
            "    \"relationships\": {" +
            "      \"author\": {" +
            "        \"data\": { \"type\": \"people\", \"id\": \"2\" }" +
            "      }" +
            "    }," +
            "    \"links\": {" +
            "      \"self\": \"http://example.com/comments/5\"" +
            "    }" +
            "  }, {" +
            "    \"type\": \"comments\"," +
            "    \"id\": \"12\"," +
            "    \"attributes\": {" +
            "      \"body\": \"I like XML better\"" +
            "    }," +
            "    \"relationships\": {" +
            "      \"author\": {" +
            "        \"data\": { \"type\": \"people\", \"id\": \"9\" }" +
            "      }" +
            "    }," +
            "    \"links\": {" +
            "      \"self\": \"http://example.com/comments/12\"" +
            "    }" +
            "  }, {" +
            "    \"type\": \"aliens\"," +
            "    \"id\": \"9\"," +
            "    \"attributes\": {" +
            "      \"first-name\": \"Dan\"," +
            "      \"last-name\": \"Gebhardt\"," +
            "      \"twitter\": \"dgeb\"," +
            "      \"age\": 20" +
            "    }" +
            "  }]" +
            "}";

    private static final String JSON_DATA_2 = "{" +
            "  \"data\": {" +
            "    \"type\": \"articles\"," +
            "    \"id\": \"1\"," +
            "    \"attributes\": {" +
            "      \"title\": \"JSON API paints my bikeshed!\"" +
            "    }" +
            "  }," +
            "  \"included\": [{" +
            "    \"type\": \"unidentified\"," +
            "    \"id\": \"9\"," +
            "    \"attributes\": {" +
            "      \"first-name\": \"Dan\"," +
            "      \"last-name\": \"Gebhardt\"," +
            "      \"twitter\": \"dgeb\"," +
            "      \"age\": 20" +
            "    }" +
            "  }]" +
            "}";

    public static Moshi moshi() {
        Moshi.Builder builder = new Moshi.Builder();
        builder.add(ResourceAdapterFactory.builder()
                .add(Article.class)
                .add(Person.class)
                .add(Comment.class)
                .build());
        return builder.build();
    }

    @Test
    public void deserialize_array_of_object() throws Exception {
        Article[] articles = moshi().adapter(Article[].class).fromJson(JSON_DATA_1);
        assertThat(articles, notNullValue());
        assertThat(articles.length, equalTo(1));
        Article a = articles[0];
        assertThat(a._id, equalTo("1"));
        assertThat(a._type, equalTo("articles"));
        assertThat(a.title, equalTo("JSON API paints my bikeshed!"));
        assertThat(a.author.get().firstName, equalTo("Dan"));
        assertThat(a.comments.get().length, equalTo(2));
    }

    @Test
    public void deserialize_object() throws Exception {
        Article article = moshi().adapter(Article.class).fromJson(JSON_DATA_2);
        assertThat(article._id, equalTo("1"));
        assertThat(article._type, equalTo("articles"));
        assertThat(article.title, equalTo("JSON API paints my bikeshed!"));
    }

    @Test(expected = JsonDataException.class)
    public void deserialize_strictly() throws Exception {
        Moshi moshi = new Moshi.Builder()
                .add(ResourceAdapterFactory.builder()
                        .add(Article.class)
                        .add(Person.class)
                        .add(Comment.class)
                        .strict()
                        .build()).build();
        moshi.adapter(Article.class).fromJson(JSON_DATA_2);
    }

    @Test
    public void serialize_object() throws Exception {
        Document document = Document.create();
        Person author = new Person();
        author._id = "5";
        author.firstName = "George";
        author.lastName = "Orwell";
        author.includeBy(document);
        Comment comment1 = new Comment();
        comment1._id = "1";
        comment1.body = "Awesome!";
        comment1.includeBy(document);
        Article article = new Article();
        article.title = "Nineteen Eighty-Four";
        article.author = HasOne.create(article, author);
        article.comments = HasMany.create(article, comment1);
        article.addTo(document);
        assertThat(moshi().adapter(Article.class).toJson(article), equalTo("{\"data\":{\"type\":\"articles\",\"attributes\":{\"title\":\"Nineteen Eighty-Four\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"5\"}},\"comments\":{\"data\":[{\"type\":\"comments\",\"id\":\"1\"}]}}},\"included\":[{\"type\":\"people\",\"id\":\"5\",\"attributes\":{\"first-name\":\"George\",\"last-name\":\"Orwell\"}},{\"type\":\"comments\",\"id\":\"1\",\"attributes\":{\"body\":\"Awesome!\"}}]}"));
    }

    @Test
    public void serialize_array_of_object() throws Exception {
        Document document = Document.create();
        Person author = new Person();
        author._id = "5";
        author.firstName = "George";
        author.lastName = "Orwell";
        author.includeBy(document);
        Comment comment1 = new Comment();
        comment1._id = "1";
        comment1.body = "Awesome!";
        comment1.includeBy(document);
        Article article = new Article();
        article.title = "Nineteen Eighty-Four";
        article.author = HasOne.create(article, author);
        article.comments = HasMany.create(article, comment1);
        article.addTo(document);
        assertThat(
                moshi().adapter(Article[].class).toJson(new Article[] { article }),
                equalTo("{\"data\":[{\"type\":\"articles\",\"attributes\":{\"title\":\"Nineteen Eighty-Four\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"people\",\"id\":\"5\"}},\"comments\":{\"data\":[{\"type\":\"comments\",\"id\":\"1\"}]}}}],\"included\":[{\"type\":\"people\",\"id\":\"5\",\"attributes\":{\"first-name\":\"George\",\"last-name\":\"Orwell\"}},{\"type\":\"comments\",\"id\":\"1\",\"attributes\":{\"body\":\"Awesome!\"}}]}"));
    }

}
