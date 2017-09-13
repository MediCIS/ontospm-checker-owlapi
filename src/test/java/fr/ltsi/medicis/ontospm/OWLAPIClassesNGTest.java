package fr.ltsi.medicis.ontospm;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author javier
 */
public class OWLAPIClassesNGTest {

    private static final String NAMESPACE = "http://medicis.univ-rennes1.fr/ontologies/ontospm/OntoSPM.owl#";
    // FIX update path of local file !
    private static final Path PATH = Paths.get("/home/javier/workspace/ontospm/ontospm-ca/OntoSPM.owl");

    private final OWLOntology ontology;
    private final OWLDataFactory factory;
    private final Util util;

    public OWLAPIClassesNGTest()
            throws
            org.semanticweb.owlapi.model.OWLOntologyCreationException {

        IRI iri = IRI.create(PATH.toFile());
        OWLOntologyDocumentSource source = new IRIDocumentSource(iri);
        OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
        // ignore imports while loading
        OWLOntologyLoaderConfiguration configuration = new OWLOntologyLoaderConfiguration();
        configuration = configuration.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        ontology = manager.loadOntologyFromOntologyDocument(source, configuration);
        factory = manager.getOWLDataFactory();
        util = Util.getInstance(ontology, factory);
    }

    /**
     * Get all classes with a valid namespace defined in the ontology source
     * file.
     *
     * @return
     */
    @DataProvider
    private Object[][] getAllClasses() {

        return ontology.classesInSignature()               
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

        return ontology.classesInSignature()
                .filter(c -> c.getIRI().getNamespace() != null
                && NAMESPACE.equals(c.getIRI().getNamespace())
                // filtering deprecated classes
                && !util.contains(c, OWLRDFVocabulary.OWL_DEPRECATED))
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

        long result = util.annotations(classe, SKOSVocabulary.PREFLABEL).count();

        Assert.assertEquals(result, 3, classe.toStringID());
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testHasUnderscoresInPrefLabel(OWLClass classe) {

        final IRI prefLabel = Util.getIRI(SKOSVocabulary.PREFLABEL);

        boolean result = ontology.annotationAssertionAxioms(classe.getIRI())
                .filter(p -> Util.compare(p.getProperty().getIRI(), prefLabel))
                .map(OWLAnnotationAssertionAxiom::getAnnotation)
                .filter(a -> a.getValue() instanceof OWLLiteral)
                .map(an -> ((OWLLiteral) an.getValue()).getLiteral())
                .reduce(false,
                        (Boolean x, String y) -> x || y.contains("_"),
                        (Boolean x, Boolean y) -> x || y);

        Assert.assertFalse(result, "A label of " + classe.toStringID() + " contains underscores.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testHasRDFSLabel(OWLClass classe) {

        boolean result = util.contains(classe, OWLRDFVocabulary.RDFS_LABEL);

        Assert.assertFalse(result, "Label is set using 'rdfs:label' in " + classe.toStringID() + " .");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testHasClassDefinition(OWLClass classe) {

        IRI definition = IRI.create("http://purl.obolibrary.org/obo/IAO_0000115");
        boolean result = util.contains(classe, definition);

        Assert.assertTrue(result, "Class " + classe.toStringID() + " has no definition.");
    }

    @Test(dataProvider = "getClasses", enabled = false)
    public void testIsDeprecatedClass(OWLClass classe) {

        boolean result = util.contains(classe, OWLRDFVocabulary.OWL_DEPRECATED);

        // TODO check if deprecated has the good annotations
        Assert.assertFalse(result, classe.toString());
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testIRIClass(OWLClass classe) {

        String result = classe.getIRI().getShortForm();
        String label = util.getSKOSPrefLabel(classe, "en");
        String expected = Util.toNormalisedLocalName(label);

        Assert.assertEquals(result, expected, "IRI local name does not match with the English label.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testEnglishLabel(OWLClass classe) {

        String result = util.getSKOSPrefLabel(classe, "en");

        Assert.assertNotNull(result, "Class " + classe.toStringID() + " has not an English label.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testFrenchLabel(OWLClass classe) {

        String result = util.getSKOSPrefLabel(classe, "fr");

        Assert.assertNotNull(result, "Class " + classe.toStringID() + " has not a French label.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testGermanLabel(OWLClass classe) {

        String result = util.getSKOSPrefLabel(classe, "de");

        Assert.assertNotNull(result, "Class " + classe.toStringID() + " has not a German label.");
    }
}
