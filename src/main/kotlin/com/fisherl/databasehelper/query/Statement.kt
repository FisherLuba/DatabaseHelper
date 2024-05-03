package com.fisherl.databasehelper.query

import com.fisherl.databasehelper.Table
import com.fisherl.databasehelper.field.Column

sealed class Statement<T>(
    val table: Table<T>,
    val columns: List<Column<*>>,
    val joins: List<TableJoin>
)

class SelectStatement<T>(
    table: Table<T>,
    columns: List<Column<*>>,
    joins: List<TableJoin>,
    val where: WhereClause?,
    val groupBy: List<Column<*>>,
    val having: HavingClause? = null, // todo
    val orderBy: OrderByClause?,
    val limit: Int?,
    val offset: Int?
) : Statement<T>(
    table,
    columns,
    joins
) {

    class Builder<T>(
        private var table: Table<T>,
        private var columns: List<Column<*>>,
        private var joins: List<TableJoin>,
        private var where: WhereClause?,
        private var orderBy: OrderByClause?,
        private var limit: Int?,
        private var offset: Int?
    ) {
        private var groupBy: List<Column<*>> = emptyList()
        private var having: HavingClause? = null

        fun groupBy(groupBy: List<Column<*>>, having: HavingClause? = null): Builder<T> {
            return this
        }

        fun build(): SelectStatement<T> {
            return SelectStatement(
                table,
                columns,
                joins,
                where,
                groupBy,
                having,
                orderBy,
                limit,
                offset
            )
        }

    }

}

class InsertStatement<T>(
    table: Table<T>,
    columns: List<Column<*>>,
    joins: List<TableJoin>,
    val selectStatement: SelectStatement<T>? = null
) : Statement<T>(
    table,
    columns,
    joins
)

class DeleteStatement<T>(
    table: Table<T>,
    joins: List<TableJoin>,
    val where: WhereClause?
) : Statement<T>(
    table,
    emptyList(),
    joins
)

class UpdateStatement<T>(
    table: Table<T>,
    columns: List<Column<*>>,
    joins: List<TableJoin>,
    val where: WhereClause?
) : Statement<T>(
    table,
    columns,
    joins
)