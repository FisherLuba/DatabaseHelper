package io.github.fisherl.databasehelper.query

import io.github.fisherl.databasehelper.Table
import io.github.fisherl.databasehelper.field.Column

data class JoinColumn(val table: Table, val column: Column<*>)

class JoinColumns private constructor(
    val leftColumns: List<JoinColumn>,
    val rightColumns: List<JoinColumn>,
    val joinTypes: List<TableJoin.Type>
) {

    class Builder {
        private val leftColumns = mutableListOf<JoinColumn>()
        private val rightColumns = mutableListOf<JoinColumn>()
        private val joinTypes = mutableListOf<TableJoin.Type>()

        fun add(leftColumn: JoinColumn, rightColumn: JoinColumn, joinType: TableJoin.Type): Builder {
            leftColumns.add(leftColumn)
            rightColumns.add(rightColumn)
            joinTypes.add(joinType)
            return this
        }

        fun build(): JoinColumns {
            return JoinColumns(leftColumns, rightColumns, joinTypes)
        }
    }

}

class TableJoin(
    val from: Table,
    val type: Type,
    val joinColumns: JoinColumns
) {

    enum class Type {
        INNER_JOIN,
        LEFT_JOIN,
        RIGHT_JOIN,
        FULL_JOIN
    }

}