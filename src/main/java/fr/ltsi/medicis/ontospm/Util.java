package fr.ltsi.medicis.ontospm;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.util.IRIComparator;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;

/**
 *
 * @author javier
 */
public class Util {

    private final OWLOntology ontology;
    private final OWLDataFactory factory;

    private Util(final OWLOntology ontology, final OWLDataFactory factory) {

        this.ontology = ontology;
        this.factory = factory;
    }

    public static Util getInstance(final OWLOntology ontology, final OWLDataFactory factory) {

        return new Util(ontology, factory);
    }

    public static String toNormalisedLocalName(final String string) {

        return string.replaceAll(" ", "_")
                .replaceAll("'", "")
                .replaceAll("-", "_").toLowerCase();
    }

    /**
     * Compare if two IRI objects are equal.
     *
     * The method delegates the comparison to the SimpleIRIShortFormProvider
     * class to perform the operation compare.
     *
     * It replaces: i1.getIRIString().equals(i2.getIRIString())
     *
     * @param i1
     * @param i2
     * @return true iff both IRI objects are the same.
     */
    public static boolean compare(final IRI i1, final IRI i2) {

        IRIShortFormProvider provider = new SimpleIRIShortFormProvider();
        IRIComparator comparator = new IRIComparator(provider);

        return comparator.compare(i1, i2) == 0;
    }

    public static <E extends Enum<E>> IRI getIRI(final E enumeration) {

        IRI result = null;
        final String name = "getIRI";
        try {

            Method method = enumeration.getClass().getMethod(name, (Class<?>[]) null);
            result = (IRI) method.invoke(enumeration);

        } catch (NoSuchMethodException
                | SecurityException
                | IllegalAccessException
                | IllegalArgumentException
                | java.lang.reflect.InvocationTargetException e) {

            // TODO
        }

        return result;
    }

    public Stream<OWLAnnotationProperty> annotations(final OWLClass classe, final IRI iri) {

        OWLAnnotationProperty property = factory.getOWLAnnotationProperty(iri);

        return ontology.annotationAssertionAxioms(classe.getIRI())
                .filter(a -> a.containsEntityInSignature(property))
                .map(OWLAnnotationAssertionAxiom::getProperty);
    }

    public <E extends Enum<E>> Stream<OWLAnnotationProperty> annotations(OWLClass classe, final E enumeration) {

        IRI iri = Util.getIRI(enumeration);

        return annotations(classe, iri);
    }

    public <E extends Enum<E>> boolean contains(final OWLClass classe, final IRI iri) {

        return ontology.annotationAssertionAxioms(classe.getIRI())
                .anyMatch(a -> Util.compare(a.getProperty().getIRI(), iri));

    }

    public <E extends Enum<E>> boolean contains(final OWLClass classe, final E enumeration) {

        final IRI iri = Util.getIRI(enumeration);

        return Util.this.contains(classe, iri);
    }

    public String getSKOSPrefLabel(final OWLClass classe, final String language) {

        final IRI prefLabel = Util.getIRI(SKOSVocabulary.PREFLABEL);

        return ontology.annotationAssertionAxioms(classe.getIRI())
                .filter(p -> Util.compare(p.getProperty().getIRI(), prefLabel))
                .map(OWLAnnotationAssertionAxiom::getAnnotation)
                .filter(a -> a.getValue() instanceof OWLLiteral)
                .map(a -> ((OWLLiteral) a.getValue()))
                .filter(l -> l.hasLang(language))
                .map(OWLLiteral::getLiteral)
                .findAny().orElse(null);
    }
}
