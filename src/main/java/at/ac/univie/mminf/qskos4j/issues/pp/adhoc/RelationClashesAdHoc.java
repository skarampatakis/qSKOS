package at.ac.univie.mminf.qskos4j.issues.pp.adhoc;

import at.ac.univie.mminf.qskos4j.issues.IssueOccursException;
import at.ac.univie.mminf.qskos4j.util.vocab.SkosOntology;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Arrays;

public class RelationClashesAdHoc implements AdHocCheckable {

    private Repository repository;

    private enum RelationType {HIERARCHICAL, ASSOCIATIVE, OTHER}

    public RelationClashesAdHoc(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void checkStatement(Statement statement) throws IssueOccursException, OpenRDFException
    {
        boolean clashFound = false;

        if (getRelationType(statement.getPredicate()) == RelationType.HIERARCHICAL) {
            clashFound = pathExists(statement.getSubject(), statement.getObject(), RelationType.ASSOCIATIVE);
        }
        else if (getRelationType(statement.getPredicate()) == RelationType.ASSOCIATIVE) {
            clashFound = pathExists(statement.getSubject(), statement.getObject(), RelationType.HIERARCHICAL);
        }

        if (clashFound) throw new IssueOccursException();
    }

    private RelationType getRelationType(URI predicate) {
        try {
            SkosOntology.getInstance().getPredicateHierarchyType(predicate);
            return RelationType.HIERARCHICAL;
        }
        catch (IllegalArgumentException e) {
            if (Arrays.asList(SkosOntology.SKOS_ASSOCIATIVE_PROPERTIES).contains(predicate)) {
                return RelationType.ASSOCIATIVE;
            }
        }

        return RelationType.OTHER;
    }

    private boolean pathExists(Resource subject, Value object, RelationType relationType)
        throws RepositoryException, QueryEvaluationException
    {
        RepositoryConnection repCon = repository.getConnection();
        try {
            String query = createPathQuery(subject, object, relationType);
            System.out.println(query);

            return repCon.prepareBooleanQuery(QueryLanguage.SPARQL, query).evaluate();
        }
        catch (MalformedQueryException e) {
            return false;
        }
        finally {
            repCon.close();
        }
    }

    private String createPathQuery(Resource subject, Value object, RelationType relationType) {
        String query = SparqlPrefix.SKOS +" ASK {";
        switch (relationType) {
            case HIERARCHICAL:
                query += "{<" +subject+ ">" +createPropertyPath(relationType)+ "<" +object+ ">}" +
                        "UNION" +
                        "{<" +object+ ">" +createPropertyPath(relationType)+ "<" +subject+ ">}";
                break;

            case ASSOCIATIVE:
                query += "<" +object+ ">" +createPropertyPath(relationType)+ "<" +subject+ ">";
                break;
        }

        query += "}";
        return query;
    }

    private String createPropertyPath(RelationType relationType) {
        String propertyPath = "";
        if (relationType == RelationType.HIERARCHICAL) {
            propertyPath = SkosOntology.getInstance().getHierarchicalPropertiesPath(SkosOntology.HierarchyType.BROADER);
        }
        else if (relationType == RelationType.ASSOCIATIVE) {
            propertyPath = "skos:related|skos:relatedMatch|^skos:related|^skos:relatedMatch";
        }

        return "(" +propertyPath+ ")+";
    }

}
