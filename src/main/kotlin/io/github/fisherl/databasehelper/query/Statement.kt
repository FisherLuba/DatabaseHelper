package io.github.fisherl.databasehelper.query

import io.github.fisherl.databasehelper.Table
import io.github.fisherl.databasehelper.field.Column

sealed class Statement constructor(
    val table: Table,
    val columns: List<Column<*>>,
    val joins: List<TableJoin>
)

class SelectStatement(
    table: Table,
    columns: List<Column<*>>,
    joins: List<TableJoin>,
    val where: WhereClause?,
    val groupBy: List<Column<*>>,
    val having: HavingClause? = null, // todo
    val orderBy: OrderByClause?,
    val limit: Int?,
    val offset: Int?
) : Statement(
    table,
    columns,
    joins
) {

    class Builder(
        private var table: Table,
        private var columns: List<Column<*>>,
        private var joins: List<TableJoin>,
        private var where: WhereClause?,
        private var orderBy: OrderByClause?,
        private var limit: Int?,
        private var offset: Int?
    ) {
        private var groupBy: List<Column<*>> = emptyList()
        private var having: HavingClause? = null

        fun groupBy(groupBy: List<Column<*>>, having: HavingClause? = null): Builder {
            return this
        }

        fun build(): SelectStatement {
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

class InsertStatement(
    table: Table,
    columns: List<Column<*>>,
    joins: List<TableJoin>,
    val selectStatement: SelectStatement? = null
) : Statement(
    table,
    columns,
    joins
)