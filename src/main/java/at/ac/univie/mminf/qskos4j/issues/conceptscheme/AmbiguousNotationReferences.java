package at.ac.univie.mminf.qskos4j.issues.conceptscheme;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.issues.concepts.AuthoritativeConcepts;
import at.ac.univie.mminf.qskos4j.issues.labels.util.AmbiguousNotation;
import at.ac.univie.mminf.qskos4j.issues.labels.util.AmbiguousNotationMultipleResources;
import at.ac.univie.mminf.qskos4j.issues.labels.util.AmbiguousNotationWithinOneResource;
import at.ac.univie.mminf.qskos4j.result.CollectionResult;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.util.*;

public class AmbiguousNotationReferences extends Issue<CollectionResult<AmbiguousNotation>> {

    private AuthoritativeConcepts authoritativeConcepts;
    private Map<Literal, Collection<Resource>> multiResourceConflicts;

    public AmbiguousNotationReferences(AuthoritativeConcepts authoritativeConcepts) {
        super(authoritativeConcepts,
                "anr",
                "Ambiguous Notation References",
                "Finds concepts with multiple or identical notations within the same concept scheme",
                IssueType.ANALYTICAL,
                new URIImpl("https://github.com/cmader/qSKOS/wiki/Quality-Issues#Ambiguous_Notation_References"));
        this.authoritativeConcepts = authoritativeConcepts;
    }

    @Override
    protected CollectionResult<AmbiguousNotation> invoke() throws OpenRDFException {
        multiResourceConflicts = new HashMap<>();
        Set<AmbiguousNotation> ambiguousNotations = new HashSet<>();

        for (Resource authConcept : authoritativeConcepts.getResult().getData()) {
            ambiguousNotations.addAll(checkNotationsForConcept(authConcept));
        }

        for (Map.Entry<Literal, Collection<Resource>> entry : multiResourceConflicts.entrySet()) {
            ambiguousNotations.add(new AmbiguousNotationMultipleResources(entry.getKey(), entry.getValue()));
        }

        return new CollectionResult<>(ambiguousNotations);
    }

    private Set<AmbiguousNotation> checkNotationsForConcept(Resource concept) throws OpenRDFException {
        Set<AmbiguousNotation> ambiguousNotations = new HashSet<>();

        ambiguousNotations.addAll(findAmbiguousNotationsWithinResource(concept));
        findAmbiguousNotationsInMultipleResources(concept);

        return ambiguousNotations;
    }

    private Set<AmbiguousNotation> findAmbiguousNotationsWithinResource(Resource concept) throws RepositoryException {
        Set<AmbiguousNotation> ambiguousNotations = new HashSet<>();
        RepositoryResult<Statement> notations = repCon.getStatements(concept, SKOS.NOTATION, null, false);

        Collection<Literal> notationsForConcept = new ArrayList<>();
        while (notations.hasNext()) {
            Literal notationLiteral = (Literal) notations.next().getObject();
            notationsForConcept.add(notationLiteral);
        }

        if (notationsForConcept.size() > 1) {
            ambiguousNotations.add(new AmbiguousNotationWithinOneResource(concept, notationsForConcept));
        }

        return ambiguousNotations;
    }

    private void findAmbiguousNotationsInMultipleResources(Resource concept) throws OpenRDFException {
        RepositoryResult<Statement> notationsForConcept = repCon.getStatements(concept, SKOS.NOTATION, null, false);
        while (notationsForConcept.hasNext()) {
            Literal notationLiteral = (Literal) notationsForConcept.next().getObject();
            getNotationClashes(concept, notationLiteral);
        }
    }

    private void getNotationClashes(Resource concept, Literal notationLiteral)
        throws OpenRDFException
    {
        RepositoryResult<Statement> conceptsWithSameNotation = repCon.getStatements(null, SKOS.NOTATION, notationLiteral, false);
        while (conceptsWithSameNotation.hasNext()) {
            Resource conflictingResource = conceptsWithSameNotation.next().getSubject();

            if (conflictingResource.equals(concept)) continue;

            if (ConceptSchemeUtil.inSameConceptScheme(concept, conflictingResource, repCon) ||
                ConceptSchemeUtil.inNoConceptScheme(concept, conflictingResource, repCon))
            {
                addToResultsMap(concept, conflictingResource, notationLiteral);
            }
        }
    }

    private void addToResultsMap(Resource resource, Resource otherResource, Literal notation) {
        Collection<Resource> resources = multiResourceConflicts.get(notation);
        if (resources == null) {
            resources = new HashSet<>();
            multiResourceConflicts.put(notation, resources);
        }
        resources.add(resource);
        resources.add(otherResource);
    }

}
