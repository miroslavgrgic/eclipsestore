package hr.ogcs.eclipsestore.hotel.domain.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity field as usable in {@link EntityFilter} criteria, and declares which
 * {@link FilterOperator}s are supported for it. Applying this to a record component also
 * applies it to the generated backing field, so records (e.g. Payment) can use it too.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Filterable {

    FilterOperator[] value() default {FilterOperator.EQUALS};

}
