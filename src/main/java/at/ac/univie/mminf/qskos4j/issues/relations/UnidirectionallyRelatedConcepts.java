package at.ac.univie.mminf.qskos4j.issues.relations;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.issues.concepts.AuthoritativeConcepts;
import at.ac.univie.mminf.qskos4j.util.Tuple;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Finds <a href="https://github.com/cmader/qSKOS/wiki/Quality-Issues#wiki-Unidirectionally_Related_Concepts">Unidirectionally Related Concepts</a>.
 */
public class UnidirectionallyRelatedConcepts extends Issue<UnidirectionallyRelatedConceptsResult> {

    private String[][] inversePropertyPairs = {
            {"skos:broader", "skos:narrower"},
            {"skos:broaderTransitive", "skos:narrowerTransitive"},
            {"skos:topConceptOf", "skos:hasTopConcept"},
            {"skos:narrowMatch", "skos:broadMatch"},
            {"skos:related", "skos:related"},
            {"skos:relatedMatch", "skos:relatedMatch"},
            {"skos:exactMatch", "skos:exactMatch"},
            {"skos:closeMatch", "skos:closeMatch"}
    };

    private final Logger logger = LoggerFactory.getLogger(UnidirectionallyRelatedConcepts.class);
    private Map<Tuple<Resource>, String> omittedInverseRelations = new HashMap<>();
    private AuthoritativeConcepts authoritativeConcepts;

    public UnidirectionallyRelatedConcepts(AuthoritativeConcepts authoritativeConcepts) {
        super(authoritativeConcepts,
                "urc",
                "Unidirectionally Related Concepts",
                "Concepts not including reciprocal relations",
                IssueType.ANALYTICAL,
                new URIImpl("https://github.com/cmader/qSKOS/wiki/Quality-Issues#unidirectionally-related-concepts"));

        this.authoritativeConcepts = authoritativeConcepts;
    }

    @Override
    protected UnidirectionallyRelatedConceptsResult invoke() throws OpenRDFException {
        String authResourceIdentifier = authoritativeConcepts.getAuthResourceIdentifier();

        for (String[] inversePropertyPair : inversePropertyPairs) {
            TupleQuery query = repCon.prepareTupleQuery(QueryLanguage.SPARQL, createOmittedRelationsQuery(inversePropertyPair));
            addToOmittedInverseRelationsMap(query.evaluate(), inversePropertyPair, authResourceIdentifier);
        }

        return new UnidirectionallyRelatedConceptsResult(omittedInverseRelations);
    }

    private String createOmittedRelationsQuery(String[] inverseRelations) {
        return SparqlPrefix.SKOS +" "+ SparqlPrefix.RDFS +
                "SELECT DISTINCT ?resource1 ?resource2 "+
                "WHERE {" +
                "{?resource1 " +inverseRelations[0]+ " ?resource2 . "+
                "FILTER NOT EXISTS {?resource2 "+inverseRelations[1]+ " ?resource1}}" +
                "UNION" +
                "{?resource1 " +inverseRelations[1]+ " ?resource2 . "+
                "FILTER NOT EXISTS {?resource2 "+inverseRelations[0]+ " ?resource1}}" +
                "}";
    }

    private void addToOmittedInverseRelationsMap(
            TupleQueryResult result,
            String[] inversePropertyPair,
            String authResourceIdentifier) throws QueryEvaluationException
    {
        while (result.hasNext()) {
            BindingSet queryResult = result.next();

            Value value1 = queryResult.getValue("resource1");
            Value value2 = queryResult.getValue("resource2");
            String inverseProperties = inversePropertyPair[0] +"/"+ inversePropertyPair[1];

            if (bothResourcesAreAuthoritative(value1, value2, authResourceIdentifier)) {
                addToMap(value1, value2, inverseProperties);
            }
        }
    }

    private boolean bothResourcesAreAuthoritative(Value res1, Value res2, String authResourceIdentifier) {
        if (authResourceIdentifier.isEmpty()) return true;

        return res1.stringValue().contains(authResourceIdentifier) &&
                res2.stringValue().contains(authResourceIdentifier);
    }

    private void addToMap(Value value1, Value value2, String inverseProperties)
    {
        try {
            Resource resource1 = (Resource) value1;
            Resource resource2 = (Resource) value2;

            omittedInverseRelations.put(
                    new Tuple<>(resource1, resource2),
                    inverseProperties);
        }
        catch (ClassCastException e) {
            logger.error("Resource expected for relation " +inverseProperties+ " (" +value1+ " <-> " +value2+ ")");
        }
    }

}
