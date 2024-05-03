package com.fisherl.databasehelper.query

import com.fisherl.databasehelper.Table
import com.fisherl.databasehelper.column.Column

data class JoinColumn(val table: Table<*>, val column: Column<*>)

class JoinColumns private constructor(
    val leftColumns: List<JoinColumn>,
    val rightColumns: List<JoinColumn>
) {

    class Builder {
        private val leftColumns = mutableListOf<JoinColumn>()
        private val rightColumns = mutableListOf<JoinColumn>()

        fun add(leftColumn: JoinColumn, rightColumn: JoinColumn): Builder {
            leftColumns.add(leftColumn)
            rightColumns.add(rightColumn)
            return this
        }

        fun build(): JoinColumns {
            return JoinColumns(leftColumns, rightColumns)
        }
    }

}

class TableJoin(
    val from: Table<*>,
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