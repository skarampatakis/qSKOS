package at.ac.univie.mminf.qskos4j.issues.conceptscheme.test;

import at.ac.univie.mminf.qskos4j.QSkos;
import at.ac.univie.mminf.qskos4j.util.IssueTestCase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;

import java.io.IOException;
import java.util.Collection;


public class OmittedTopConceptsTest extends IssueTestCase {

	private QSkos qSkosMissingTopConcepts;
	
	@Before
	public void setUp() throws OpenRDFException, IOException {
		qSkosMissingTopConcepts = setUpIssue("missingTopConcepts.rdf");
	}
	
	@Test
	public void testConceptSchemesWithoutTopConceptsCount() throws OpenRDFException {
		Collection<Resource> conceptSchemes = qSkosMissingTopConcepts.findOmittedTopConcepts().getData();
		Assert.assertEquals(2, conceptSchemes.size());
	}
	
}