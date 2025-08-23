package com.bgsoftware.wildloaders.api.data;



import com.bgsoftware.wildloaders.api.objects.DatabaseEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class DatabaseFilter {

    private static DatabaseFilterEmpty EMPTY_FILTER;

    public static DatabaseFilter fromFilter(String filterKey, Object filterValue) {
        return new DatabaseFilterSingle(filterKey, filterValue);
    }

    public static DatabaseFilter fromFilters(List<DatabaseEntry<String, Object>> filters) {
        if (filters.isEmpty()) {
            if (EMPTY_FILTER == null)
                EMPTY_FILTER = new DatabaseFilterEmpty();

            return EMPTY_FILTER;
        } else if (filters.size() == 1) {
            DatabaseEntry<String, Object> filter = filters.get(0);
            return fromFilter(filter.getKey(), filter.getValue());
        } else {
            return new DatabaseFilterList(filters);
        }
    }

    protected DatabaseFilter() {
    }

    public abstract void forEach(BiConsumer<String, Object> consumer);

    public abstract Collection<DatabaseEntry<String, Object>> getFilters();

    private static class DatabaseFilterList extends DatabaseFilter {

        private final Collection<DatabaseEntry<String, Object>> filters;

        DatabaseFilterList(Collection<DatabaseEntry<String, Object>> filters) {
            this.filters = filters;
        }

        @Override
        public void forEach(BiConsumer<String, Object> consumer) {
            filters.forEach(databaseEntry -> consumer.accept(databaseEntry.getKey(), databaseEntry.getValue()));
        }

        @Override
        public Collection<DatabaseEntry<String, Object>> getFilters() {
            return Collections.unmodifiableCollection(filters);
        }

    }

    private static class DatabaseFilterEmpty extends DatabaseFilter {

        @Override
        public void forEach(BiConsumer<String, Object> consumer) {
            // Do nothing.
        }

        @Override
        public Collection<DatabaseEntry<String, Object>> getFilters() {
            return Collections.emptyList();
        }

    }

    private static class DatabaseFilterSingle extends DatabaseFilter {

        private final String filterKey;
        private final Object filterValue;

        DatabaseFilterSingle(String filterKey, Object filterValue) {
            this.filterKey = filterKey;
            this.filterValue = filterValue;
        }

        @Override
        public void forEach(BiConsumer<String, Object> consumer) {
            consumer.accept(filterKey, filterValue);
        }

        @Override
        public Collection<DatabaseEntry<String, Object>> getFilters() {
            return Collections.singleton(new DatabaseEntry<>(filterKey, filterValue));
        }

    }


}
