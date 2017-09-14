package fr.ltsi.medicis.ontospm;

import java.lang.reflect.Method;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.IRIComparator;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

/**
 *
 * @author javier
 */
public class OSCUtil {

    /**
     * Transform a String to a valid name of the last segment path of an IRI.
     *
     * The transformation performs: (1) replace a space with underscores (2)
     * remove single quotes, and (3) replace a dash with underscore.
     *
     * @param string
     * @return a (OntoSPM) normalised string of last segment of an
     * {@link org.semanticweb.owlapi.model.IRI} path.
     */
    public static String toNormalisedSergmentPath(final String string) {

        return string.replaceAll(" ", "_")
                .replaceAll("'", "")
                .replaceAll("-", "_").toLowerCase();
    }

    // public static Function<String, String> toSegmentPath = OSCUtil::toNormalisedSergmentPath;
        
    /**
     * Compare if two IRI objects are equals.
     *
     * The method delegates the comparison to the
     * {@link org.semanticweb.owlapi.util.SimpleIRIShortFormProvider} class to
     * perform the operation compare.
     *
     * It replaces and enforces:
     * {@code i1.getIRIString().equals(i2.getIRIString())}
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

    /**
     * Get the IRI from an enumeration.
     *
     * The enumeration is an extention of
     * {@link org.semanticweb.owlapi.model.HasIRI}. This enables to get any IRI
     * from the enumerations defined in the package:
     * {@link org.semanticweb.owlapi.vocab}
     *
     * @param <E> any enumeration from {@link org.semanticweb.owlapi.vocab}.
     * @param enumeration
     * @return the {@link org.semanticweb.owlapi.model.IRI} associated to the
     * enumeration.
     */
    public static <E extends Enum<E> & HasIRI> IRI getIRI(final E enumeration) {

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

            throw new RuntimeException(e);
        }

        return result;
    }
}
