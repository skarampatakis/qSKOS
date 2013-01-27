package at.ac.univie.mminf.qskos4j.issues.outlinks;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.result.general.CollectionResult;
import at.ac.univie.mminf.qskos4j.result.general.NumberResult;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.TupleQueryResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by christian
 * Date: 26.01.13
 * Time: 15:23
 */
public class HttpURIs extends Issue<CollectionResult<URI>> {

    private Set<URI> httpURIs = Collections.EMPTY_SET;
    private Set<String> invalidResources = Collections.EMPTY_SET;

    public HttpURIs() {
        super("huc",
              "HTTP URI Count",
              "Counts the total number of HTTP URIs",
              IssueType.STATISTICAL
        );
    }

    @Override
    protected CollectionResult<URI> invoke() throws OpenRDFException {

        TupleQueryResult result = vocabRepository.query(createIRIQuery());

        while (result.hasNext()) {
            Value iri = result.next().getValue("iri");
            addToUrlList(iri);
        }

        return new CollectionResult<URI>(httpURIs);
    }

    private String createIRIQuery() {
        return "SELECT DISTINCT ?iri "+
            "FROM <" +vocabRepository.getVocabContext()+ "> "+
                "WHERE {" +
                    "{{?s ?p ?iri .} UNION "+
                    "{?iri ?p ?o .} UNION "+
                    "{?s ?iri ?p .}} "+
                    "FILTER isIRI(?iri)" +
                "}";
    }

    private void addToUrlList(Value iri) {
        try {
            URI uri = new URI(iri.stringValue());

            if (uri.getScheme().startsWith("http")) {
                httpURIs.add(pruneFragment(uri));
            }
        }
        catch (URISyntaxException e) {
            invalidResources.add(iri.toString());
        }
    }

    private URI pruneFragment(URI uri) throws URISyntaxException
    {
        if (uri.getFragment() != null) {
            int hashIndex = uri.toString().indexOf("#");
            return new URI(uri.toString().substring(0, hashIndex));
        }
        return uri;
    }
}
