package at.ac.univie.mminf.qskos4j.issues.outlinks;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.result.CollectionResult;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by christian
 * Date: 26.01.13
 * Time: 15:23
 */
public class HttpURIs extends Issue<CollectionResult<URI>> {

    private Set<URI> httpURIs = new HashSet<>();
    private Set<String> invalidResources = new HashSet<>();

    public HttpURIs() {
        super("huc",
              "HTTP URI Count",
              "Counts the total number of HTTP URIs",
              IssueType.STATISTICAL
        );
    }

    @Override
    protected CollectionResult<URI> invoke() throws OpenRDFException {
        RepositoryResult<Statement> result = repCon.getStatements(null, null, null, false, (Resource) null);
        while (result.hasNext()) {
            Statement st = result.next();

            Collection<Value> tripleValues = new ArrayList<>();
            tripleValues.addAll(Arrays.asList(st.getSubject(), st.getObject(), st.getPredicate()));

            for (Value value : tripleValues) {
                if (value instanceof org.openrdf.model.URI) addToUrlList(value);
            }
        }

        return new CollectionResult<>(httpURIs);
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
