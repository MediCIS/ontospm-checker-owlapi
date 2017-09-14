package fr.ltsi.medicis.ontospm;

import static fr.ltsi.medicis.ontospm.OSCOntology.NAMESPACE;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author javier
 */
public class OWLAPIClassesNGTest {

    // FIX update path of local file !
    private static final Path PATH = Paths.get("/home/javier/workspace/ontospm/ontospm-ca/OntoSPM.owl");

    private final OSCOntology ontology;

    public OWLAPIClassesNGTest() {

        OSCManager manager = OSCManager.getInstance(PATH);
        ontology = OSCOntology.getInstance(manager.getOntology(), manager.getFactory());
    }

    /**
     * Get all classes with a valid namespace defined in the ontology source
     * file.
     *
     * @return
     */
    @DataProvider
    private Object[][] getAllClasses() {

        return ontology.classes()
                .filter(c -> c.getIRI().getNamespace() != null)
                .map(x -> new OWLClass[]{x})
                .toArray(Object[][]::new);
    }

    /**
     * Get classes that are not deprecated, with valid namespace, and are
     * defined in OntoSPM.
     *
     * @return
     */
    @DataProvider
    private Object[][] getClasses() {

        return ontology.classes()
                .filter(c -> c.getIRI().getNamespace() != null
                && NAMESPACE.equals(c.getIRI().getNamespace())
                // filtering deprecated classes
                && !ontology.hasAnnotation(c, OWLRDFVocabulary.OWL_DEPRECATED))
                .map(x -> new OWLClass[]{x})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getAllClasses", enabled = false)
    public void testHasOntoSPMNamespace(OWLClass classe) {

        boolean result = !NAMESPACE.equals(classe.getIRI().getNamespace());

        Assert.assertFalse(result, "Class: " + classe.toStringID() + " defined as part of OntoSPM.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testClassWithAllLabels(OWLClass classe) {

        long result = ontology.preferredLabels(classe).count();

        Assert.assertEquals(result, 3, classe.toStringID());
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testHasUnderscoresInPreferredLabel(OWLClass classe) {

        boolean result = ontology.preferredLabels(classe)
                .map(literal -> literal.getLiteral())
                .reduce(false,
                        (Boolean x, String y) -> x || y.contains("_"),
                        (Boolean x, Boolean y) -> x || y);

        Assert.assertFalse(result, "A label of " + classe.toStringID() + " contains underscores.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testHasRDFSLabel(OWLClass classe) {

        boolean result = ontology.hasAnnotation(classe, OWLRDFVocabulary.RDFS_LABEL);

        Assert.assertFalse(result, "Label is set using 'rdfs:label' in " + classe.toStringID() + " .");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testHasClassDefinition(OWLClass classe) {

        // annotation <IAO:definition> 
        IRI definition = IRI.create("http://purl.obolibrary.org/obo/IAO_0000115");
        boolean result = ontology.hasAnnotation(classe, definition);

        Assert.assertTrue(result, "Class " + classe.toStringID() + " has no definition.");
    }

    @Test(dataProvider = "getClasses", enabled = false)
    public void testIsDeprecatedClass(OWLClass classe) {

        boolean result = ontology.hasAnnotation(classe, OWLRDFVocabulary.OWL_DEPRECATED);

        // TODO check if deprecated has the good annotations
        Assert.assertFalse(result, classe.toString());
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testIRIClass(OWLClass classe) {

        String result = classe.getIRI().getShortForm();
        String label = ontology.getPrefLabel(classe, "en");
        String expected = OSCUtil.toNormalisedSergmentPath(label);        

        Assert.assertEquals(result, expected, "IRI local name does not match with the English label.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testEnglishLabel(OWLClass classe) {

        String result = ontology.getPrefLabel(classe, "en");

        Assert.assertNotNull(result, "Class " + classe.toStringID() + " has not an English label.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testFrenchLabel(OWLClass classe) {

        String result = ontology.getPrefLabel(classe, "fr");

        Assert.assertNotNull(result, "Class " + classe.toStringID() + " has not a French label.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testGermanLabel(OWLClass classe) {

        String result = ontology.getPrefLabel(classe, "de");

        Assert.assertNotNull(result, "Class " + classe.toStringID() + " has not a German label.");
    }
}
