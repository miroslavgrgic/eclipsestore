package hr.ogcs.eclipsestore.hotel.domain.filter;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Applies caller-supplied criteria maps against entity fields annotated with {@link Filterable}.
 * A criteria key is either a bare field name (equals match) or a field name suffixed with
 * "_gte", "_lte", "_contains" or "_in" to select a different {@link FilterOperator}.
 */
public final class EntityFilter {

    private static final String GTE_SUFFIX = "_gte";
    private static final String LTE_SUFFIX = "_lte";
    private static final String CONTAINS_SUFFIX = "_contains";
    private static final String IN_SUFFIX = "_in";

    private EntityFilter() {
    }

    public static <T> List<T> filter(List<T> entities, Map<String, Object> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return List.copyOf(entities);
        }
        return entities.stream()
                .filter(entity -> matches(entity, criteria))
                .collect(Collectors.toList());
    }

    public static <K, V> Map<K, V> filterValues(Map<K, V> entities, Map<String, Object> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return new LinkedHashMap<>(entities);
        }
        Map<K, V> result = new LinkedHashMap<>();
        entities.forEach((key, value) -> {
            if (matches(value, criteria)) {
                result.put(key, value);
            }
        });
        return result;
    }

    private static boolean matches(Object entity, Map<String, Object> criteria) {
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            if (!matchesSingle(entity, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesSingle(Object entity, String key, Object rawValue) {
        String fieldName = key;
        FilterOperator operator = FilterOperator.EQUALS;
        if (key.endsWith(GTE_SUFFIX)) {
            fieldName = key.substring(0, key.length() - GTE_SUFFIX.length());
            operator = FilterOperator.GREATER_OR_EQUAL;
        } else if (key.endsWith(LTE_SUFFIX)) {
            fieldName = key.substring(0, key.length() - LTE_SUFFIX.length());
            operator = FilterOperator.LESS_OR_EQUAL;
        } else if (key.endsWith(CONTAINS_SUFFIX)) {
            fieldName = key.substring(0, key.length() - CONTAINS_SUFFIX.length());
            operator = FilterOperator.CONTAINS;
        } else if (key.endsWith(IN_SUFFIX)) {
            fieldName = key.substring(0, key.length() - IN_SUFFIX.length());
            operator = FilterOperator.IN;
        }

        Field field = findFilterableField(entity.getClass(), fieldName, operator);
        Object actual;
        try {
            field.setAccessible(true);
            actual = field.get(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read field '" + fieldName + "' on " + entity.getClass(), e);
        }

        return switch (operator) {
            case EQUALS -> Objects.equals(actual, coerce(rawValue, field.getType()));
            case CONTAINS -> actual != null && rawValue != null
                    && actual.toString().toLowerCase().contains(rawValue.toString().toLowerCase());
            case GREATER_OR_EQUAL -> actual != null && compare(actual, coerce(rawValue, field.getType())) >= 0;
            case LESS_OR_EQUAL -> actual != null && compare(actual, coerce(rawValue, field.getType())) <= 0;
            case IN -> {
                if (!(rawValue instanceof Collection<?> values)) {
                    throw new IllegalArgumentException("Criteria for '" + key + "' must be a list of allowed values");
                }
                yield values.stream()
                        .map(value -> coerce(value, field.getType()))
                        .anyMatch(value -> Objects.equals(actual, value));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static int compare(Object actual, Object expected) {
        if (!(actual instanceof Comparable<?> comparable)) {
            throw new IllegalArgumentException("Field of type " + actual.getClass() + " does not support range filtering");
        }
        return ((Comparable<Object>) comparable).compareTo(expected);
    }

    private static Field findFilterableField(Class<?> type, String fieldName, FilterOperator operator) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.getName().equals(fieldName)) {
                    continue;
                }
                Filterable filterable = field.getAnnotation(Filterable.class);
                if (filterable == null) {
                    throw new IllegalArgumentException(
                            "Field '" + fieldName + "' on " + type.getSimpleName() + " is not filterable");
                }
                if (Arrays.stream(filterable.value()).noneMatch(op -> op == operator)) {
                    throw new IllegalArgumentException(
                            "Field '" + fieldName + "' on " + type.getSimpleName() + " does not support " + operator + " filtering");
                }
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown filter field '" + fieldName + "' on " + type.getSimpleName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object coerce(Object raw, Class<?> targetType) {
        if (raw == null || targetType.isInstance(raw)) {
            return raw;
        }
        if (targetType == UUID.class) {
            return UUID.fromString(raw.toString());
        }
        if (targetType == LocalDate.class) {
            return LocalDate.parse(raw.toString());
        }
        if (targetType == Date.class) {
            return Date.from(Instant.parse(raw.toString()));
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(raw.toString());
        }
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, raw.toString());
        }
        if (targetType == int.class || targetType == Integer.class) {
            return raw instanceof Number number ? number.intValue() : Integer.parseInt(raw.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            return raw instanceof Number number ? number.longValue() : Long.parseLong(raw.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            return raw instanceof Number number ? number.doubleValue() : Double.parseDouble(raw.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return raw instanceof Boolean bool ? bool : Boolean.parseBoolean(raw.toString());
        }
        return raw;
    }
}
