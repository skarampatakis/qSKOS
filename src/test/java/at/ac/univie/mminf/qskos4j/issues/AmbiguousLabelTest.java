package at.ac.univie.mminf.qskos4j.issues;

import java.io.IOException;
import java.util.Collection;

import at.ac.univie.mminf.qskos4j.issues.labelconflict.LabelConflict;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;

import at.ac.univie.mminf.qskos4j.QSkos;
import org.openrdf.model.URI;


public class AmbiguousLabelTest extends IssueTestCase {

	private QSkos qSkosAmbiguousLabels;
	
	@Before
	public void setUp() throws OpenRDFException, IOException {
		qSkosAmbiguousLabels = setUpInstance("ambiguousLabels.rdf");
	}
	
	@Test
	public void testUniquePrefLabels() throws OpenRDFException {
		Collection<LabelConflict> ambiguousResources = qSkosAmbiguousLabels.findAmbiguouslyPreflabeledResources().getData();
		
		Assert.assertTrue(uriSuffixIsPartOfConflict(ambiguousResources, "conceptA"));
		Assert.assertTrue(uriSuffixIsPartOfConflict(ambiguousResources, "conceptA2"));
        Assert.assertTrue(uriSuffixIsPartOfConflict(ambiguousResources, "conceptA3"));
        Assert.assertTrue(uriSuffixIsPartOfConflict(ambiguousResources, "conceptA4"));
		Assert.assertFalse(uriSuffixIsPartOfConflict(ambiguousResources, "conceptB"));
		Assert.assertFalse(uriSuffixIsPartOfConflict(ambiguousResources, "conceptC"));
		Assert.assertFalse(uriSuffixIsPartOfConflict(ambiguousResources, "conceptG"));
	}
	
	@Test 
	public void testDisjointLabels() throws OpenRDFException {
        Collection<LabelConflict> ambiguousResources = qSkosAmbiguousLabels.findDisjointLabelsViolations().getData();
		
		Assert.assertTrue(uriSuffixIsPartOfConflict(ambiguousResources, "conceptD"));
		Assert.assertTrue(uriSuffixIsPartOfConflict(ambiguousResources, "conceptF"));
        Assert.assertFalse(uriSuffixIsPartOfConflict(ambiguousResources, "conceptE"));
        Assert.assertFalse(uriSuffixIsPartOfConflict(ambiguousResources, "conceptG"));
    }
	
	private boolean uriSuffixIsPartOfConflict(
        Collection<LabelConflict> conflicts,
		String suffix)
	{
        if (conflicts != null) {
            for (LabelConflict conflict : conflicts) {
                for (URI resource : conflict.getAffectedResources()) {
                    if (resource.stringValue().endsWith(suffix)) {
                        return true;
                    }
                }
            }
        }

		return false;
	}
	
}
