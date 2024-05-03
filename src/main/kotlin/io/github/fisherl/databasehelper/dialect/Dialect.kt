package io.github.fisherl.databasehelper.dialect

import io.github.fisherl.databasehelper.Table
import io.github.fisherl.databasehelper.query.InsertStatement
import io.github.fisherl.databasehelper.query.SelectStatement
import io.github.fisherl.databasehelper.query.Statement

sealed class DatabaseDialect {

    abstract fun createTableStatement(table: Table): String

    abstract fun createSelectStatement(selectStatement: SelectStatement): String

    abstract fun createInsertStatement(insertStatement: InsertStatement): String

}

private val FIX_SPACES_REGEX = "[ ]+".toRegex()

data object PostgresDialect : DatabaseDialect() {

    override fun createTableStatement(table: Table): String {
        val columnStatements = table.columns.joinToString(", ") {
            "${it.shortName} ${it.type.name}" + "${it.storageType?.let { storageType -> "STORAGE $storageType" } ?: ""} " + "${it.compressionType?.let { compressionType -> "COMPRESSION $compressionType" } ?: ""} " + "${it.collation?.let { collation -> "COLLATE $collation" } ?: ""} " + it.constraints.joinToString(
                " "
            ) { constraint ->
                constraint.name.replace(
                    '_', ' '
                )
            }

        }
        val output =
            "CREATE TABLE " + "${if (table.ifNotExists) "IF NOT EXISTS" else ""} " + "${table.name} ${if (table.temporary) "TEMP" else ""} " + "${if (table.unlogged) "UNLOGGED" else ""} " + "($columnStatements);"
        return output.replace(FIX_SPACES_REGEX, " ")
    }

    override fun createSelectStatement(selectStatement: SelectStatement): String {
        val columns = createColumnsString(selectStatement)
        val from = selectStatement.table.name
        val join = createJoinString(selectStatement)
        val where = createWhereString(selectStatement)
        val orderBy = createOrderByString(selectStatement)
        val output =
            "SELECT $columns FROM $from ${selectStatement.where?.name ?: ""} " +
                    "$join " +
                    "$where " +
                    "${selectStatement.orderBy?.name ?: ""} $orderBy"
        return output.replace(FIX_SPACES_REGEX, " ").trimEnd() + ";"
    }

    override fun createInsertStatement(insertStatement: InsertStatement): String {
        val columns = createColumnsString(insertStatement)
        val into = insertStatement.table.name
        val values = insertStatement.columns.joinToString(", ") { "?" }
        val join = createJoinString(insertStatement)
        val select = insertStatement.selectStatement?.let { createSelectStatement(it) } ?: ""
        val output =
            "INSERT INTO $into (${columns}) VALUES (${values}) " +
                    "$join " +
                    select
        return output.replace(FIX_SPACES_REGEX, " ").trimEnd() + if (select.isEmpty()) ";" else ""
    }

    private fun createColumnsString(statement: Statement): String {
        return statement.columns.joinToString(", ") { it.name }
    }

    private fun createJoinString(statement: Statement): String {
        return statement.joins.joinToString(" ") {
            "${
                it.type.name.replace(
                    '_',
                    ' '
                )
            } ${it.from.name} ON ${it.joinColumns.leftColumns[0].column.name} = ${it.joinColumns.rightColumns[0].column.name}"
        }
    }

    private fun createWhereString(selectStatement: SelectStatement): String {
        return selectStatement.where?.parts?.joinToString(" ") {
            "${it.operator?.name ?: ""} ${it.output}"
        } ?: ""
    }

    private fun createOrderByString(selectStatement: SelectStatement): String {
        return selectStatement.orderBy?.parts?.joinToString(", ") {
            "${it.output} ${it.operator?.name ?: ""}"
        } ?: ""
    }
}