package projetMichelin.services.indexation;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

//recherche par mots-clés dans les commentaires
@Service
public class IndexService {

    private DirectoryReader reader; 
    private IndexSearcher searcher;  
    private final IndexWriter writer;   
    private final QueryParser parser;     

    public IndexService() throws IOException {
        this("index");
    }

    //ce sont les tests qui l'utilise
    public IndexService(String chemin) throws IOException {
        Directory dossier = FSDirectory.open(Paths.get(chemin));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        this.parser = new QueryParser("content", analyzer);
        this.writer = new IndexWriter(dossier, new IndexWriterConfig(analyzer));

        if (!DirectoryReader.indexExists(dossier)) {
            this.writer.commit();
        }
        this.reader = DirectoryReader.open(dossier);
        this.searcher = new IndexSearcher(reader);
    }

    // Ajoute le commentaire d'une évaluation dans l'index
    public void indexComment(Integer evaluationId, String comment) {
        try {
            Document document = new Document();
            document.add(new StringField("evaluationId", String.valueOf(evaluationId), Field.Store.YES));
            document.add(new TextField("content", comment, Field.Store.YES));

            writer.addDocument(document);
            writer.commit();
            rafraichir();
        } catch (IOException e) {
            throw new RuntimeException("Erreur pendant l'indexation", e);
        }
    }

    //on delete une évaluation de l'index
    public void deleteComment(Integer evaluationId) {
        try {
            writer.deleteDocuments(new Term("evaluationId", String.valueOf(evaluationId)));
            writer.commit();
            rafraichir();
        } catch (IOException e) {
            throw new RuntimeException("Erreur pendant la suppression", e);
        }
    }

    //cherche les mots clés et renvoie les id des évaluations trouvées
    public List<Integer> searchComment(String motsCles) {
        try {
            Query query = parser.parse(QueryParser.escape(motsCles));
            TopDocs resultats = searcher.search(query, 100);
            List<Integer> ids = new ArrayList<>();
            for (ScoreDoc trouve : resultats.scoreDocs) {
                String id = searcher.storedFields().document(trouve.doc).get("evaluationId");
                ids.add(Integer.valueOf(id));
            }
            return ids;
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Erreur pendant la recherche", e);
        }
    }

    // on recharge le lecteur en fait après un ajout ou une suppression
    private void rafraichir() throws IOException {
        DirectoryReader nouveau = DirectoryReader.openIfChanged(reader);
        if (nouveau != null) {
            reader.close();
            reader = nouveau;
            searcher = new IndexSearcher(reader);
        }
    }
    //Quand l'appli s'arrête, on referme l'index pour libérer write.lock
    @PreDestroy
    public void fermer() throws IOException {
        reader.close();
        writer.close();
    }
}