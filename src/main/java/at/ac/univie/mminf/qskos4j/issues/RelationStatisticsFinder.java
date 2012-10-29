package at.ac.univie.mminf.qskos4j.issues;

import java.util.Collection;
import java.util.HashSet;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import at.ac.univie.mminf.qskos4j.result.general.CollectionResult;
import at.ac.univie.mminf.qskos4j.result.general.NumberResult;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import at.ac.univie.mminf.qskos4j.util.vocab.VocabRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationStatisticsFinder extends Issue {

    private final Logger logger = LoggerFactory.getLogger(RelationStatisticsFinder.class);

	public RelationStatisticsFinder(VocabRepository vocabRepository) {
		super(vocabRepository);
	}
	
	public NumberResult<Long> findLexicalRelationsCount(Collection<URI> allConcepts) 
	{
		long relationsCount = 0; 
		
		for (URI concept : allConcepts) {
            try {
			    TupleQueryResult result = vocabRepository.query(createLexicalLabelQuery(concept));
                relationsCount += countResults(result);
            }
            catch (OpenRDFException e) {
                logger.error("Error finding labels for concept '" +concept+ "'");
            }
		}
		
		return new NumberResult<Long>(relationsCount);
	}
	
	private String createLexicalLabelQuery(URI concept) {
		return SparqlPrefix.SKOS +" "+ SparqlPrefix.SKOSXL +" "+ SparqlPrefix.RDFS +
			"SELECT DISTINCT ?skoslabelType ?literal WHERE {" +
				"<" +concept.stringValue()+ "> ?skoslabelType ?literal ." +
				"{?skoslabelType rdfs:subPropertyOf* skos:prefLabel}" +
				"UNION" +
				"{?skoslabelType rdfs:subPropertyOf* skos:altLabel}" +
				"UNION" +
				"{?skoslabelType rdfs:subPropertyOf* skos:hiddenLabel}" +
			"}";	
	}
			
	private long countResults(TupleQueryResult result) throws QueryEvaluationException 
	{
		long count = 0;
		
		while (result.hasNext()) {
			count++;
			result.next();
		}
		
		return count;
	}
	
	public NumberResult<Long> findSemanticRelationsCount() 
		throws OpenRDFException
	{
        TupleQueryResult result = vocabRepository.query(createSemanticRelationsQuery());
        return new NumberResult<Long>(countResults(result));
	}

	private String createSemanticRelationsQuery() {
		return SparqlPrefix.SKOS +" "+ SparqlPrefix.RDFS +
			"SELECT * WHERE {" +
				"?concept ?relationType ?otherConcept ."+
				"?relationType rdfs:subPropertyOf* skos:semanticRelation ."+
			"}";	
	}
	
	public NumberResult<Long> findAggregationRelationsCount() throws OpenRDFException
	{
		TupleQueryResult result = vocabRepository.query(createAggregationRelationsQuery());
		return new NumberResult<Long>(countResults(result));
	}
	
	private String createAggregationRelationsQuery() {
		return SparqlPrefix.SKOS +" "+ SparqlPrefix.RDFS +
			"SELECT * WHERE {" +
				"?res1 ?relationType ?res2 ."+
				"{?relationType rdfs:subPropertyOf* skos:topConceptOf}" +
				"UNION" +
				"{?relationType rdfs:subPropertyOf* skos:hasTopConcept}" +
				"UNION" +
				"{?relationType rdfs:subPropertyOf* skos:inScheme}"+
				"UNION" +
				"{?relationType rdfs:subPropertyOf* skos:member}"+
				"UNION" +
				"{?relationType rdfs:subPropertyOf* skos:memberList}"+
			"}";
	}
	
	public CollectionResult<Value> findConceptSchemes() throws OpenRDFException
	{
		TupleQueryResult result = vocabRepository.query(createConceptSchemeQuery());		
		return new CollectionResult<Value>(identifyResources(result));
	}
	
	private String createConceptSchemeQuery() {
		return SparqlPrefix.SKOS +" "+ SparqlPrefix.RDFS +" "+ SparqlPrefix.RDF +
			"SELECT DISTINCT ?resource WHERE {" +
				"{?resource rdf:type/rdfs:subClassOf* skos:ConceptScheme}" +
				"UNION" +
				"{?resource ?hasTopConcept ?concept . ?hasTopConcept rdfs:subPropertyOf* skos:hasTopConcept}" +
				"UNION" +
				"{?concept ?topConceptOf ?resource . ?topConceptOf rdfs:subPropertyOf* skos:topConceptOf}" +
				"UNION" +
				"{?concept ?inScheme ?resource . ?inScheme rdfs:subPropertyOf* skos:inScheme}"+
			"}";	
	}
	
	public NumberResult<Long> findCollectionCount() throws OpenRDFException
	{
		TupleQueryResult result = vocabRepository.query(createCollectionsQuery());
		return new NumberResult<Long>(countResults(result));
	}
	
	public Collection<Value> identifyResources(TupleQueryResult result) throws QueryEvaluationException
	{
		Collection<Value> allResources = new HashSet<Value>();
		
		while (result.hasNext()) {
            allResources.add(result.next().getValue("resource"));
		}
		
		return allResources;
	}
	
	private String createCollectionsQuery() {
		return SparqlPrefix.SKOS +" "+ SparqlPrefix.RDFS +" "+ SparqlPrefix.RDF +
			"SELECT DISTINCT ?collection WHERE {" +
				"{?collection rdf:type/rdfs:subClassOf* skos:Collection .}" +
				"UNION" +
				"{?collection ?hasMember ?resource . ?hasMember rdfs:subPropertyOf* skos:member}" +
				"UNION" +
				"{?collection ?memberList ?resource . ?memberList rdfs:subPropertyOf* skos:memberList}" +
			"}";
	}
}
