package at.ac.univie.mminf.qskos4j;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;

public class AmbiguousLabelTest extends QSkosTestCase {

	@Test
	public void testUniquePrefLabels() {
		Map<URI, Set<String>> ambiguousConcepts = qSkosAmbiguousLabels.findNotUniquePrefLabels();
		
		Assert.assertNotNull(getEntryForUriSuffix(ambiguousConcepts, "conceptA"));
		Assert.assertNotNull(getEntryForUriSuffix(ambiguousConcepts, "conceptA2"));
		Assert.assertNull(getEntryForUriSuffix(ambiguousConcepts, "conceptB"));
		Assert.assertNull(getEntryForUriSuffix(ambiguousConcepts, "conceptC"));
	}
	
	@Test 
	public void testDisjointLabels() {
		Map<URI, Set<String>> ambiguousConcepts = qSkosAmbiguousLabels.findNotDisjointLabels();
		
		Assert.assertNotNull(getEntryForUriSuffix(ambiguousConcepts, "conceptD"));
		Assert.assertNull(getEntryForUriSuffix(ambiguousConcepts, "conceptE"));
		Assert.assertNotNull(getEntryForUriSuffix(ambiguousConcepts, "conceptF"));
	}
	
	private Set<String> getEntryForUriSuffix(
		Map<URI, Set<String>> map,
		String suffix)
	{
		for (URI resource : map.keySet()) {
			if (resource.stringValue().endsWith(suffix)) {
				return map.get(resource);
			}
		}
		return null;
	}
	
}