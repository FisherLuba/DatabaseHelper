package com.fisherl.databasehelper.query

import com.fisherl.databasehelper.field.Column

enum class LogicalOperator {
    AND,
    OR
}

enum class OrderDirection {
    ASC,
    DESC
}

sealed class Clause<T>(
    val name: String,
    val parts: List<ClausePart<T>> = mutableListOf()
)

data class ClausePart<T>(val output: String, val operator: T? = null)

class ClauseInput<T> private constructor(
    val column: Column<T>,
    val output: String
) {

    companion object {

        fun <T> column(column: Column<T>): ClauseInput<T> {
            return ClauseInput(column, column.name)
        }

        fun <T> count(column: Column<T>): ClauseInput<T> {
            return ClauseInput(column, "COUNT(${column.name})")
        }

        fun <T> sum(column: Column<T>): ClauseInput<T> {
            return ClauseInput(column, "SUM(${column.name})")
        }

        fun <T> avg(column: Column<T>): ClauseInput<T> {
            return ClauseInput(column, "AVG(${column.name})")
        }

        fun <T> min(column: Column<T>): ClauseInput<T> {
            return ClauseInput(column, "MIN(${column.name})")
        }

        fun <T> max(column: Column<T>): ClauseInput<T> {
            return ClauseInput(column, "MAX(${column.name})")
        }

    }
    
    fun format(value: T): String {
        return column.format(value)
    }

}

sealed class WhereClause private constructor(parts: MutableList<ClausePart<LogicalOperator>>) :
    Clause<LogicalOperator>("WHERE", parts) {

    companion object {
        fun builder(): Incomplete {
            return Incomplete(mutableListOf(), null)
        }
    }

    class Complete(parts: MutableList<ClausePart<LogicalOperator>>) : WhereClause(parts) {
        fun and(): Incomplete {
            return Incomplete(this.parts.toMutableList(), LogicalOperator.AND)
        }

        fun or(): Incomplete {
            return Incomplete(this.parts.toMutableList(), LogicalOperator.OR)
        }
    }

    class Incomplete(
        private val parts: MutableList<ClausePart<LogicalOperator>>,
        private val operator: LogicalOperator?
    ) {

        private fun toComplete(output: String): Complete {
            parts.add(ClausePart(output, this.operator))
            return Complete(parts)
        }

        fun <T> equal(left: ClauseInput<T>, right: T): Complete {
            return toComplete("${left.output} = ${left.format(right)}")
        }

        fun <T> equal(left: ClauseInput<T>, right: Column<T>): Complete {
            return toComplete("${left.output} = ${right.name}")
        }

        fun <T> notEqual(left: ClauseInput<T>, right: T): Complete {
            return toComplete("${left.output} != ${left.format(right)}")
        }

        fun <T> notEqual(left: ClauseInput<T>, right: Column<T>): Complete {
            return toComplete("${left.output} != ${right.name}")
        }

        fun <T> greaterThan(left: ClauseInput<T>, right: T): Complete {
            return toComplete("${left.output} > ${left.format(right)}")
        }

        fun <T> greaterThan(left: ClauseInput<T>, right: Column<T>): Complete {
            return toComplete("${left.output} > ${right.name}")
        }

        fun <T> greaterThanOrEqual(left: ClauseInput<T>, right: T): Complete {
            return toComplete("${left.output} >= ${left.format(right)}")
        }

        fun <T> greaterThanOrEqual(left: ClauseInput<T>, right: Column<T>): Complete {
            return toComplete("${left.output} >= ${right.name}")
        }

        fun <T> lessThan(left: ClauseInput<T>, right: T): Complete {
            return toComplete("${left.output} < ${left.format(right)}")
        }

        fun <T> lessThan(left: ClauseInput<T>, right: Column<T>): Complete {
            return toComplete("${left.output} < ${right.name}")
        }

        fun <T> lessThanOrEqual(left: ClauseInput<T>, right: T): Complete {
            return toComplete("${left.output} <= ${left.format(right)}")
        }

        fun <T> lessThanOrEqual(left: ClauseInput<T>, right: Column<T>): Complete {
            return toComplete("${left.output} <= ${right.name}")
        }

        fun <T> like(left: ClauseInput<T>, right: T): Complete {
            return toComplete("${left.output} LIKE ${left.format(right)}")
        }

        fun <T> `in`(left: ClauseInput<T>, right: List<T>): Complete {
            return toComplete("${left.output} IN (${right.joinToString(", ")})")
        }

        fun <T> `in`(left: ClauseInput<T>, right: Column<T>): Complete {
            return toComplete("${left.output} IN (${right.name})")
        }

        fun <T> isNull(left: ClauseInput<T>): Complete {
            return toComplete("${left.output} IS NULL")
        }

        fun <T> isNotNull(left: ClauseInput<T>): Complete {
            return toComplete("${left.output} IS NOT NULL")
        }

    }

}

sealed class OrderByClause(parts: MutableList<ClausePart<OrderDirection>>) :
    Clause<OrderDirection>("ORDER BY", parts) {

    companion object {
        fun builder(): Complete {
            return Complete(mutableListOf())
        }
    }

    class Complete(parts: MutableList<ClausePart<OrderDirection>>) : OrderByClause(parts) {
        fun asc(): Incomplete {
            return Incomplete(this.parts.toMutableList(), OrderDirection.ASC)
        }

        fun desc(): Incomplete {
            return Incomplete(this.parts.toMutableList(), OrderDirection.DESC)
        }
    }

    class Incomplete(
        private val parts: MutableList<ClausePart<OrderDirection>>,
        private val direction: OrderDirection
    ) {

        private fun toComplete(output: String): Complete {
            parts.add(ClausePart(output, this.direction))
            return Complete(parts)
        }

        fun <T> column(column: Column<T>): Complete {
            return toComplete(column.name)
        }

    }

}

sealed class HavingClause {


}