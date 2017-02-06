package fr.ltsi.medicis.ontospm;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.SKOS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author javier
 */
public class ClassesNGTest {

    private static final String NAMESPACE = "http://medicis.univ-rennes1.fr/ontologies/ontospm/OntoSPM.owl#";
    private static final Path PATH = Paths.get("/home/javier/workspace/ontospm-ca/OntoSPM.owl");

    private final OntModel model;
    private final Set<OntClass> functions;
    private final Set<OntClass> deprecatedFunctions;
    private final Set<OntClass> categories;

    public ClassesNGTest()
            throws
            java.io.IOException {

        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.getDocumentManager().setProcessImports(true);
        InputStream stream = FileManager.get().open(PATH.toString());
        model.read(stream, null);

        OntClass function = model.getOntClass(NAMESPACE + "function_of_instrument");
        functions = function.listSubClasses(true).toList().stream()
                .filter(x -> x.getProperty(OWL2.deprecated) == null)
                .collect(Collectors.toSet());

        deprecatedFunctions = function.listSubClasses(true).toList().stream()
                .filter(x -> x.getProperty(OWL2.deprecated) != null)
                .collect(Collectors.toSet());

        categories = Arrays.asList(
                "operative_specimen",
                "surgical_material",
                "surgical_instrument",
                "medical_equipment").stream()
                .map(category -> model.getOntClass(NAMESPACE + category))
                .collect(Collectors.toSet());
    }

    @DataProvider()
    private Object[][] getClasses() {

        return model.listClasses().toList().stream()
                .filter(x -> x.getNameSpace() != null
                        && x.getProperty(OWL2.deprecated) == null
                        && NAMESPACE.equals(x.getNameSpace()))
                .map(x -> new OntClass[]{x})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testClassRDFSLabel(OntClass classe) {

        String label = classe.getLabel(null);

        assertNull(label, "label is set using 'rdfs:label' in " + classe.getLocalName());
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testClassWithoutLabel(OntClass classe) {

        Statement statement = classe.getProperty(SKOS.prefLabel);

        assertNotNull(statement, "label of " + classe.getLocalName() + " is not defined with SKOS.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testClassLabelWithUnderScore(OntClass classe) {

        boolean result = classe.listProperties(SKOS.prefLabel).toList().stream()
                .map(x -> x.getObject().asLiteral().getValue().toString())
                .reduce(false,
                        (Boolean x, String y) -> x = x || y.contains("_"),
                        (Boolean x, Boolean y) -> x || y);

        assertFalse(result, "A label of class " + classe.getLocalName() + " contains underscores.");
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testClassAllLabels(OntClass classe) {

        long count = classe.listProperties(SKOS.prefLabel).toList().size();

        assertEquals(count, 3, "labels in all languages are not set properly in " + classe.getLocalName());
    }

    @Test(dataProvider = "getClasses", enabled = true)
    public void testClassIRI(OntClass classe) {

        String iri = classe.getLocalName();
        String label = classe.listProperties(SKOS.prefLabel).toList().stream()
                .filter(x -> x.getObject().asLiteral().getLanguage().equals("en"))
                .map(x -> x.getObject().asLiteral().getValue().toString())
                .findAny().orElse(null);

        if (label != null) {

            label = label
                    .replaceAll(" ", "_")
                    .replaceAll("'", "")
                    .replaceAll("-", "_").toLowerCase();
        }

        assertEquals(label, iri, "IRI local name does not match with label.");
    }

    @DataProvider
    private Object[][] getSurgicalContinuant() {

        return categories.stream()
                .flatMap(category -> model.listClasses().toList().stream()
                        .filter(instrument -> instrument.getNameSpace() != null
                                && NAMESPACE.equals(instrument.getNameSpace())
                                && instrument.hasSuperClass(category)))
                .map(x -> new OntClass[]{x})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getSurgicalContinuant", enabled = true)
    public void testInstrumentHasFunction(OntClass instrument) {

        long counter = 0;
        if (instrument.getEquivalentClass() == null || !instrument.hasSubClass()) {

            counter = getCount(instrument.listSuperClasses(true).toList(), functions);
        }

        List<String> filteredConinuants = Arrays.asList("surgical_furniture", "medical_device");
        if (categories.contains(instrument) || (filteredConinuants.contains(instrument.getLocalName()))) {

            counter++;
        }

        assertTrue(counter > 0, "'" + instrument.getLocalName() + "' has no function.");
    }

    @Test(dataProvider = "getSurgicalContinuant", enabled = true)
    public void TestInstrumentsWithDeprecatedFunctions(OntClass instrument) {

        long counter = getCount(instrument.listSuperClasses(true).toList(), deprecatedFunctions);

        assertFalse(counter > 0, "'" + instrument.getLocalName() + "' has deprecated function(s).");
    }

    private long getCount(List<OntClass> list, final Set<OntClass> functions) {

        return list.stream()
                .filter(x -> x.isRestriction())
                .map(OntClass::asRestriction)
                .filter(restriction
                        -> restriction.isAllValuesFromRestriction()
                        || restriction.isSomeValuesFromRestriction())
                .map(restriction -> restriction.isAllValuesFromRestriction()
                                ? restriction.asAllValuesFromRestriction().getAllValuesFrom()
                                : restriction.asSomeValuesFromRestriction().getSomeValuesFrom())
                .filter(r -> !r.isAnon())
                .map(resouce -> model.getOntClass(resouce.getURI()))
                .filter(c -> functions.contains(c))
                .count();
    }
}
