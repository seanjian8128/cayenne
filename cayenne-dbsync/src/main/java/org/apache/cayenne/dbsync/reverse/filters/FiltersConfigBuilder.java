/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */
package org.apache.cayenne.dbsync.reverse.filters;

import org.apache.cayenne.dbsync.reverse.dbimport.Catalog;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeRelationship;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.PatternParam;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * @since 4.0
 */
public final class FiltersConfigBuilder {

    private final ReverseEngineering engineering;

    public FiltersConfigBuilder(ReverseEngineering engineering) {
        this.engineering = engineering;
    }

    public FiltersConfig build() {
        compact();

        return new FiltersConfig(transformCatalogs(engineering.getCatalogs()));
    }

    private CatalogFilter[] transformCatalogs(Collection<Catalog> catalogs) {
        CatalogFilter[] catalogFilters = new CatalogFilter[catalogs.size()];
        int i = 0;
        for (Catalog catalog : catalogs) {
            catalogFilters[i] = new CatalogFilter(catalog.getName(), transformSchemas(catalog.getSchemas()));
            i++;
        }

        return catalogFilters;
    }

    private SchemaFilter[] transformSchemas(Collection<Schema> schemas) {
        SchemaFilter[] schemaFilters = new SchemaFilter[schemas.size()];
        int i = 0;
        for (Schema schema : schemas) {
            schemaFilters[i] = new SchemaFilter(schema.getName(),
                    new TableFilter(transformIncludeTable(schema.getIncludeTables()),
                            transformExcludeTable(schema.getExcludeTables())),
                    transform(schema.getIncludeProcedures(), schema.getExcludeProcedures()));
            i++;
        }

        return schemaFilters;
    }

    private SortedSet<Pattern> transformExcludeTable(Collection<ExcludeTable> excludeTables) {
        SortedSet<Pattern> res = new TreeSet<>(PatternFilter.PATTERN_COMPARATOR);
        for (ExcludeTable exclude : excludeTables) {
            res.add(PatternFilter.pattern(exclude.getPattern()));
        }
        return res;
    }

    private SortedSet<IncludeTableFilter> transformIncludeTable(Collection<IncludeTable> includeTables) {
        SortedSet<IncludeTableFilter> includeTableFilters = new TreeSet<>();
        for (IncludeTable includeTable : includeTables) {
            includeTableFilters.add(new IncludeTableFilter(includeTable.getPattern(),
                    transform(includeTable.getIncludeColumns(), includeTable.getExcludeColumns(), includeTable.getExcludeRelationship())));

        }

        return includeTableFilters;
    }

    private PatternFilter transform(Collection<? extends PatternParam> include, Collection<? extends PatternParam> exclude) {
        PatternFilter filter = new PatternFilter();

        for (PatternParam patternParam : include) {
            filter.include(patternParam.getPattern());
        }

        for (PatternParam patternParam : exclude) {
            filter.exclude(patternParam.getPattern());
        }

        return filter;

    }

    private PatternFilter transform(Collection<? extends PatternParam> include, Collection<? extends PatternParam> exclude, Collection<? extends PatternParam> excludeRel) {
        PatternFilter filter = new PatternFilter();

        for (PatternParam patternParam : include) {
            filter.include(patternParam.getPattern());
        }

        for (PatternParam patternParam : exclude) {
            filter.exclude(patternParam.getPattern());
        }

        for(PatternParam patternParam : excludeRel){
            filter.exclude(patternParam.getPattern());
        }

        return filter;

    }

    /**
     * Goal of this method transform ReverseEngineering config into more regular form
     * From
     * <pre>
     *      ReverseEngineering
     *          Catalog
     *              Schema
     *                  IncludeTable
     *                      IncludeColumn
     *                      ExcludeColumn
     *                  ExcludeTable
     *                  IncludeProcedures
     *                  ExcludeProcedures
     *                  IncludeColumn
     *                  ExcludeColumn
     *              IncludeTable
     *                  IncludeColumn
     *                  ExcludeColumn
     *              ExcludeTable
     *              IncludeProcedures
     *              ExcludeProcedures
     *              IncludeColumn
     *              ExcludeColumn
     *          Schema
     *              IncludeTable
     *                  IncludeColumn
     *                  ExcludeColumn
     *              ExcludeTable
     *              IncludeProcedures
     *              ExcludeProcedures
     *              IncludeColumn
     *              ExcludeColumn
     *          IncludeTable
     *              IncludeColumn
     *              ExcludeColumn
     *          ExcludeTable
     *          IncludeProcedures
     *          ExcludeProcedures
     *          IncludeColumn
     *          ExcludeColumn
     * </pre>
     * Into
     * <pre>
     *  ReverseEngineering
     *          Catalog
     *              Schema
     *                  IncludeTable
     *                      IncludeColumn
     *                      ExcludeColumn
     *                  ExcludeTable
     *                  IncludeProcedures
     *                  ExcludeProcedures
     * </pre>
     */
    void compact() {
        addEmptyElements();

        compactColumnFilters();
        compactRelationshipFilters();
        compactTableFilter();
        compactProcedureFilter();
        compactSchemas();

        clearGlobalFilters();
    }

    private void compactSchemas() {
        for (Catalog catalog : engineering.getCatalogs()) {
            catalog.getSchemas().addAll(engineering.getSchemas());
        }
    }

    private void compactProcedureFilter() {
        Collection<IncludeProcedure> engIncludeProcedures = engineering.getIncludeProcedures();
        Collection<ExcludeProcedure> engExcludeProcedures = engineering.getExcludeProcedures();

        for (Catalog catalog : engineering.getCatalogs()) {
            Collection<IncludeProcedure> catalogIncludeProcedures = catalog.getIncludeProcedures();
            Collection<ExcludeProcedure> catalogExcludeProcedures = catalog.getExcludeProcedures();

            for (Schema schema : catalog.getSchemas()) {
                schema.getIncludeProcedures().addAll(engIncludeProcedures);
                schema.getIncludeProcedures().addAll(catalogIncludeProcedures);

                schema.getExcludeProcedures().addAll(engExcludeProcedures);
                schema.getExcludeProcedures().addAll(catalogExcludeProcedures);
            }
        }

        for (Schema schema : engineering.getSchemas()) {
            schema.getIncludeProcedures().addAll(engIncludeProcedures);
            schema.getExcludeProcedures().addAll(engExcludeProcedures);
        }
    }

    private void compactTableFilter() {
        Collection<IncludeTable> engIncludeTables = engineering.getIncludeTables();
        Collection<ExcludeTable> engExcludeTables = engineering.getExcludeTables();

        for (Catalog catalog : engineering.getCatalogs()) {
            Collection<IncludeTable> catalogIncludeTables = catalog.getIncludeTables();
            Collection<ExcludeTable> catalogExcludeTables = catalog.getExcludeTables();

            for (Schema schema : catalog.getSchemas()) {
                schema.getIncludeTables().addAll(engIncludeTables);
                schema.getIncludeTables().addAll(catalogIncludeTables);

                schema.getExcludeTables().addAll(engExcludeTables);
                schema.getExcludeTables().addAll(catalogExcludeTables);
            }
        }

        for (Schema schema : engineering.getSchemas()) {
            schema.getIncludeTables().addAll(engIncludeTables);
            schema.getExcludeTables().addAll(engExcludeTables);
        }
    }

    private void compactRelationshipFilters() {
        Collection<ExcludeRelationship> engExcludeRelationship = engineering.getExcludeRelationship();

        for(Catalog catalog : engineering.getCatalogs()){
            Collection<ExcludeRelationship> catalogExcludeRelationship = catalog.getExcludeRelationship();

            for(Schema schema : catalog.getSchemas()){
                Collection<ExcludeRelationship> schemaExcludeRelationship = schema.getExcludeRelationship();

                for(IncludeTable includeTable : schema.getIncludeTables()) {
                    includeTable.getExcludeRelationship().addAll(engExcludeRelationship);
                    includeTable.getExcludeRelationship().addAll(catalogExcludeRelationship);
                    includeTable.getExcludeRelationship().addAll(schemaExcludeRelationship);
                }
            }

            for(IncludeTable includeTable : catalog.getIncludeTables()) {
                includeTable.getExcludeRelationship().addAll(engExcludeRelationship);
                includeTable.getExcludeRelationship().addAll(catalogExcludeRelationship);
            }
        }

        for (Schema schema : engineering.getSchemas()) {
            Collection<ExcludeRelationship> schemaExcludeRelationship = schema.getExcludeRelationship();

            for (IncludeTable includeTable : schema.getIncludeTables()) {
                includeTable.getExcludeRelationship().addAll(engExcludeRelationship);
                includeTable.getExcludeRelationship().addAll(schemaExcludeRelationship);
            }
        }

        for (IncludeTable includeTable : engineering.getIncludeTables()) {
            includeTable.getExcludeRelationship().addAll(engExcludeRelationship);
        }
    }

    private void compactColumnFilters() {
        Collection<IncludeColumn> engIncludeColumns = engineering.getIncludeColumns();
        Collection<ExcludeColumn> engExcludeColumns = engineering.getExcludeColumns();

        for (Catalog catalog : engineering.getCatalogs()) {
            Collection<IncludeColumn> catalogIncludeColumns = catalog.getIncludeColumns();
            Collection<ExcludeColumn> catalogExcludeColumns = catalog.getExcludeColumns();

            for (Schema schema : catalog.getSchemas()) {
                Collection<IncludeColumn> schemaIncludeColumns = schema.getIncludeColumns();
                Collection<ExcludeColumn> schemaExcludeColumns = schema.getExcludeColumns();

                for (IncludeTable includeTable : schema.getIncludeTables()) {
                    includeTable.getIncludeColumns().addAll(engIncludeColumns);
                    includeTable.getIncludeColumns().addAll(catalogIncludeColumns);
                    includeTable.getIncludeColumns().addAll(schemaIncludeColumns);

                    includeTable.getExcludeColumns().addAll(engExcludeColumns);
                    includeTable.getExcludeColumns().addAll(catalogExcludeColumns);
                    includeTable.getExcludeColumns().addAll(schemaExcludeColumns);
                }
            }

            for (IncludeTable includeTable : catalog.getIncludeTables()) {
                includeTable.getIncludeColumns().addAll(engIncludeColumns);
                includeTable.getIncludeColumns().addAll(catalogIncludeColumns);

                includeTable.getExcludeColumns().addAll(engExcludeColumns);
                includeTable.getExcludeColumns().addAll(catalogExcludeColumns);
            }
        }

        for (Schema schema : engineering.getSchemas()) {
            Collection<IncludeColumn> schemaIncludeColumns = schema.getIncludeColumns();
            Collection<ExcludeColumn> schemaExcludeColumns = schema.getExcludeColumns();

            for (IncludeTable includeTable : schema.getIncludeTables()) {
                includeTable.getIncludeColumns().addAll(engIncludeColumns);
                includeTable.getIncludeColumns().addAll(schemaIncludeColumns);

                includeTable.getExcludeColumns().addAll(engExcludeColumns);
                includeTable.getExcludeColumns().addAll(schemaExcludeColumns);
            }
        }

        for (IncludeTable includeTable : engineering.getIncludeTables()) {
            includeTable.getIncludeColumns().addAll(engIncludeColumns);
            includeTable.getExcludeColumns().addAll(engExcludeColumns);
        }
    }

    private void clearGlobalFilters() {
        for(Catalog catalog : engineering.getCatalogs()) {
            catalog.clearIncludeTables();
            catalog.clearExcludeTables();
            catalog.clearIncludeProcedures();
            catalog.clearExcludeProcedures();
            catalog.clearIncludeColumns();
            catalog.clearExcludeColumns();
            catalog.clearExcludeRelationships();

            for (Schema schema : catalog.getSchemas()) {
                schema.clearIncludeColumns();
                schema.clearExcludeColumns();
                schema.clearExcludeRelationships();
            }
        }

        engineering.clearIncludeTables();
        engineering.clearExcludeTables();
        engineering.clearIncludeProcedures();
        engineering.clearExcludeProcedures();
        engineering.clearIncludeColumns();
        engineering.clearExcludeColumns();
        engineering.clearExcludeRelationships();

        engineering.getSchemas().clear();
    }

    private void addEmptyElements() {
        if (engineering.getCatalogs().isEmpty()) {
            engineering.addCatalog(new Catalog());
        }

        for (Catalog catalog : engineering.getCatalogs()) {
            if (catalog.getSchemas().isEmpty() && engineering.getSchemas().isEmpty()) {
                catalog.addSchema(new Schema());
            }

            for (Schema schema : catalog.getSchemas()) {
                if (schema.getIncludeTables().isEmpty() && catalog.getIncludeTables().isEmpty() && engineering.getIncludeTables().isEmpty()) {
                    schema.addIncludeTable(new IncludeTable());
                }
            }
        }

        for (Schema schema : engineering.getSchemas()) {
            if (schema.getIncludeTables().isEmpty() && engineering.getIncludeTables().isEmpty()) {
                schema.addIncludeTable(new IncludeTable());
            }
        }
    }
}
