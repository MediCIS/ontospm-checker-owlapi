package fr.ltsi.medicis.ontospm;

import java.util.stream.Stream;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
// import org.semanticweb.owlapi.util.OWLClassLiteralCollector;

/**
 *
 * @author javier
 */
public class OSCOntology {

    public static final String NAMESPACE = "http://medicis.univ-rennes1.fr/ontologies/ontospm/OntoSPM.owl#";

    private final OWLOntology ontology;
    private final OWLDataFactory factory;

    private OSCOntology(final OWLOntology ontology, final OWLDataFactory factory) {

        this.ontology = ontology;
        this.factory = factory;
    }

    public static OSCOntology getInstance(final OWLOntology ontology, final OWLDataFactory factory) {

        return new OSCOntology(ontology, factory);
    }

    public Stream<OWLClass> classes() {

        return ontology.classesInSignature();
    }

    /**
     * Get a stream of all annotation assertion axioms associated to an entity.
     *
     * @param entity {@link org.semanticweb.owlapi.model.OWLEntity}
     * @return {@link java.util.stream.Stream} of
     * {@link org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom}
     */
    private Stream<OWLAnnotationAssertionAxiom> annotationAssertions(final OWLEntity entity) {

        return ontology.annotationAssertionAxioms(entity.getIRI());
    }

    /**
     * Get all annotation properties in a class containing a specific entity.
     * @param entity
     * @param iri
     * @return 
     */
    private Stream<OWLAnnotationAssertionAxiom> filteredAnnotationAssertions(final OWLEntity entity, final IRI iri) {

        OWLEntity property = factory.getOWLAnnotationProperty(iri);

        return this.annotationAssertions(entity)
                .filter(a -> a.containsEntityInSignature(property));
    }

    public Stream<OWLAnnotationProperty> annotationProperties(final OWLEntity entity, final IRI iri) {

        return this.filteredAnnotationAssertions(entity, iri)
                .map(OWLAnnotationAssertionAxiom::getProperty);
    }

    public <E extends Enum<E> & HasIRI> Stream<OWLAnnotationProperty> annotationProperties(OWLEntity entity, final E enumeration) {

        return this.annotationProperties(entity, OSCUtil.getIRI(enumeration));
    }

    public <E extends Enum<E>> boolean hasAnnotation(final OWLEntity entity, final IRI iri) {

//       OWLEntity property = factory.getOWLAnnotationProperty(iri);
//        return this.annotationAssertions(entity)
//                .anyMatch(a -> a.containsEntityInSignature(property));
        return this.filteredAnnotationAssertions(entity, iri).count() != 0;
    }

    public <E extends Enum<E> & HasIRI> boolean hasAnnotation(final OWLEntity entity, final E enumeration) {

        return this.hasAnnotation(entity, OSCUtil.getIRI(enumeration));
    }

    public <E extends Enum<E> & HasIRI> Stream<OWLAnnotationValue> annotationValues(final OWLEntity entity, final IRI iri) {

        return this.filteredAnnotationAssertions(entity, iri)
                .map(a -> a.getAnnotation().getValue());
    }

    public <E extends Enum<E> & HasIRI> Stream<OWLAnnotationValue> annotationValues(final OWLEntity entity, final E enumeration) {

        return this.annotationValues(entity, OSCUtil.getIRI(enumeration));
    }

    public Stream<OWLLiteral> preferredLabels(final OWLEntity entity) {

        return this.annotationValues(entity, SKOSVocabulary.PREFLABEL)
                //.map(a-> a.asLiteral());
                .filter(OWLLiteral.class::isInstance)
                .map(OWLLiteral.class::cast);
    }

    public String getPreferredLabel(final OWLEntity entity, final String language) {

        return this.preferredLabels(entity)
                .filter(literal -> literal.hasLang(language))
                .map(OWLLiteral::getLiteral)
                .findAny().orElse(null);
    }
}
