package at.ac.univie.mminf.qskos4j.issues.skosintegrity.test;

import at.ac.univie.mminf.qskos4j.QSkos;
import at.ac.univie.mminf.qskos4j.issues.skosintegrity.UndefinedSkosResources;
import at.ac.univie.mminf.qskos4j.util.IssueTestCase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;

import java.io.IOException;
import java.util.Collection;


public class UndefinedSkosResourcesTest extends IssueTestCase {
	
	private UndefinedSkosResources undefinedSkosResourcesInConcepts, undefinedSkosResourcesInDeprecatedAndIllegal;
	
	@Before
	public void setUp() throws OpenRDFException, IOException {
        undefinedSkosResourcesInConcepts = (UndefinedSkosResources) setUpIssue(
            "concepts.rdf",
            new UndefinedSkosResources());

        undefinedSkosResourcesInDeprecatedAndIllegal = (UndefinedSkosResources) setUpIssue(
            "deprecatedAndIllegalTerms.rdf",
            new UndefinedSkosResources());
	}
	
	@Test
	public void testUndefinedSkosResourcesCount_1() throws OpenRDFException {
		Collection<URI> undefRes = undefinedSkosResourcesInConcepts.getResult().getData();
		Assert.assertEquals(3, undefRes.size());
	}

	@Test
	public void testUndefinedSkosResourcesCount_2() throws OpenRDFException {
		Collection<URI> undefRes = undefinedSkosResourcesInDeprecatedAndIllegal.getResult().getData();
		Assert.assertEquals(12, undefRes.size());
	}
	
}