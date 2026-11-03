package projetMichelin.services.indexation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IndexServiceTest {

    private IndexService indexService;

    @BeforeEach
    void setUp() throws IOException {
        //un nouveau dossier pour chaque test
        indexService = new IndexService("target/index-test/" + UUID.randomUUID());
    }

    @Test
    void trouveUnCommentaire() {
        indexService.indexComment(1, "Les meilleures lasagnes de Nancy");

        List<Integer> ids = indexService.searchComment("lasagnes");

        assertEquals(List.of(1), ids);
    }

    @Test
    void ignoreLesMajuscules() {
        indexService.indexComment(1, "Les meilleures lasagnes de Nancy");

        assertEquals(List.of(1), indexService.searchComment("LASAGNES"));
    }

    @Test
    void plusieursMotsCles() {
        indexService.indexComment(1, "Les meilleures lasagnes de Nancy");
        indexService.indexComment(2, "Sushi très frais");

        List<Integer> ids = indexService.searchComment("lasagnes sushi");

        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));
    }

    @Test
    void motAbsent() {
        indexService.indexComment(1, "Les meilleures lasagnes de Nancy");

        assertTrue(indexService.searchComment("pizza").isEmpty());
    }

    @Test
    void plusTrouveApresSuppression() {
        indexService.indexComment(1, "Les meilleures lasagnes de Nancy");
        indexService.deleteComment(1);

        assertTrue(indexService.searchComment("lasagnes").isEmpty());
    }
}