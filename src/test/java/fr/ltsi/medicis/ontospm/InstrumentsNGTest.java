package fr.ltsi.medicis.ontospm;

import org.semanticweb.owlapi.model.OWLClass;
import org.testng.annotations.Test;

/**
 *
 * @author javier
 */
public class InstrumentsNGTest {

    @Test(dataProvider = "getSurgicalContinuant", enabled = false)
    public void testInstrumentHasFunction(OWLClass continuant) {

    }

    @Test(dataProvider = "getSurgicalContinuant", enabled = false)
    public void TestInstrumentsWithDeprecatedFunctions(OWLClass continuant) {

    }
}
