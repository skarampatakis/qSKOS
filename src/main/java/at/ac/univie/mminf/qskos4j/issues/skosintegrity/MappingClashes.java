package at.ac.univie.mminf.qskos4j.issues.skosintegrity;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.result.CollectionResult;
import at.ac.univie.mminf.qskos4j.util.Tuple;
import at.ac.univie.mminf.qskos4j.util.TupleQueryResultUtil;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;

import java.util.Collection;
import java.util.HashSet;

/**
 * Finds <a href="https://github.com/cmader/qSKOS/wiki/Quality-Issues#wiki-Mapping_Clashes">Exact vs. Associative and Hierarchical Mapping Clashes</a>.
 */
public class MappingClashes extends Issue<CollectionResult<Tuple<Resource>>> {

    public MappingClashes() {
        super("mc",
              "Mapping Clashes",
              "Covers condition S46 from the SKOS reference document (Exact vs. Associative and Hierarchical Mapping Clashes)",
              IssueType.ANALYTICAL,
              new URIImpl("https://github.com/cmader/qSKOS/wiki/Quality-Issues#mapping-clashes")
        );
    }

    @Override
    protected CollectionResult<Tuple<Resource>> invoke() throws OpenRDFException {
        TupleQuery query = repCon.prepareTupleQuery(QueryLanguage.SPARQL, createExVsAssMappingQuery());

        Collection<Tuple<Resource>> valuePairs = TupleQueryResultUtil.createCollectionOfResourcePairs(
            query.evaluate(),
            "concept1", "concept2");
        Collection<Tuple<Resource>> distinctPairs = new HashSet<Tuple<Resource>>();
        distinctPairs.addAll(valuePairs);

        return new CollectionResult<Tuple<Resource>>(distinctPairs);
    }

    private String createExVsAssMappingQuery() {
        return SparqlPrefix.SKOS +
            "SELECT ?concept1 ?concept2 WHERE {" +
                "?concept1 (skos:exactMatch|^skos:exactMatch)+ ?concept2 ."+
                "?concept1 skos:broadMatch|skos:narrowMatch|skos:relatedMatch ?concept2 ." +
                "}";
    }
}
