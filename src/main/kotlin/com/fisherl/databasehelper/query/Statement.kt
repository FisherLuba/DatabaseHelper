package com.fisherl.databasehelper.query

import com.fisherl.databasehelper.Table
import com.fisherl.databasehelper.column.Column

sealed class Statement<T>(
    val table: Table<T>,
    val columns: List<Column<*>>,
    val joins: List<TableJoin>
)

class SelectStatement<T> private constructor(
    table: Table<T>,
    columns: List<Column<*>>,
    joins: List<TableJoin> = emptyList(),
    val where: WhereClause? = null,
    val groupBy: List<Column<*>> = emptyList(),
    val having: HavingClause? = null, // todo
    val orderBy: OrderByClause? = null,
    val limit: Int? = null,
    val offset: Int? = null
) : Statement<T>(
    table,
    columns,
    joins
) {

    class Builder<T>(
        private var table: Table<T>,
        private var columns: List<Column<*>>,
        private var joins: List<TableJoin> = listOf(),
        private var where: WhereClause? = null,
        private var orderBy: OrderByClause? = null,
        private var limit: Int? = null,
        private var offset: Int? = null
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
    joins: List<TableJoin> = emptyList(),
    val selectStatement: SelectStatement<T>? = null
) : Statement<T>(
    table,
    columns,
    joins
)

class DeleteStatement<T>(
    table: Table<T>,
    joins: List<TableJoin> = emptyList(),
    val where: WhereClause? = null
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