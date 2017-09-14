package fr.ltsi.medicis.ontospm;

import java.nio.file.Path;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author javier
 */
public class OSCManager {

    private final OWLOntology ontology;
    private final OWLDataFactory factory;

    private OSCManager(final Path path)
            throws
            org.semanticweb.owlapi.model.OWLOntologyCreationException {

        IRI iri = IRI.create(path.toFile());
        OWLOntologyDocumentSource source = new IRIDocumentSource(iri);
        OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
        // ignore imports while loading
        OWLOntologyLoaderConfiguration configuration = new OWLOntologyLoaderConfiguration();
        configuration = configuration.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        ontology = manager.loadOntologyFromOntologyDocument(source, configuration);
        factory = manager.getOWLDataFactory();
    }

    public static OSCManager getInstance(final Path path) {

        OSCManager manager = null;

        try {

            manager = new OSCManager(path);
        } catch (org.semanticweb.owlapi.model.OWLOntologyCreationException e) {

            throw new RuntimeException(e);
        }

        return manager;
    }

    public OWLOntology getOntology() {

        return ontology;
    }

    public OWLDataFactory getFactory() {

        return factory;
    }
}
