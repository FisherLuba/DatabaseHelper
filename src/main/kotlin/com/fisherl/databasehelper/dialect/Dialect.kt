package com.fisherl.databasehelper.dialect

import com.fisherl.databasehelper.Table
import com.fisherl.databasehelper.query.DeleteStatement
import com.fisherl.databasehelper.query.InsertStatement
import com.fisherl.databasehelper.query.SelectStatement
import com.fisherl.databasehelper.query.Statement
import com.fisherl.databasehelper.query.UpdateStatement
import com.fisherl.databasehelper.query.WhereClause

sealed class DatabaseDialect {

    abstract fun <T> createTableStatement(table: Table<T>): String

    abstract fun <T> createSelectStatement(selectStatement: SelectStatement<T>): String

    abstract fun <T> createInsertStatement(insertStatement: InsertStatement<T>): String

    abstract fun <T> createDeleteStatement(deleteStatement: DeleteStatement<T>): String

    abstract fun <T> createUpdateStatement(updateStatement: UpdateStatement<T>): String

}

private val FIX_SPACES_REGEX = "[ ]+".toRegex()

data object PostgresDialect : DatabaseDialect() {

    override fun <T> createTableStatement(table: Table<T>): String {
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

    override fun <T> createSelectStatement(selectStatement: SelectStatement<T>): String {
        val columns = createColumnsString(selectStatement)
        val from = selectStatement.table.name
        val join = createJoinString(selectStatement)
        val where = selectStatement.where?.let { createWhereString(it) } ?: ""
        val orderBy = createOrderByString(selectStatement)
        val output =
            "SELECT $columns FROM $from " +
                    "$join " +
                    "$where " +
                    "${selectStatement.orderBy?.name ?: ""} $orderBy"
        return output.replace(FIX_SPACES_REGEX, " ").trimEnd() + ";"
    }

    override fun <T> createInsertStatement(insertStatement: InsertStatement<T>): String {
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

    override fun <T> createDeleteStatement(deleteStatement: DeleteStatement<T>): String {
        val from = deleteStatement.table.name
        val join = createJoinString(deleteStatement)
        val where = deleteStatement.where?.let { createWhereString(it) } ?: ""
        val output = "DELETE FROM $from $join $where"
        return output.replace(FIX_SPACES_REGEX, " ").trimEnd() + ";"
    }

    override fun <T> createUpdateStatement(updateStatement: UpdateStatement<T>): String {
        val table = updateStatement.table.name
        val set = updateStatement.columns.joinToString(", ") { "${it.name} = ?" }
        val join = createJoinString(updateStatement)
        val where = updateStatement.where?.let { createWhereString(it) } ?: ""
        val output = "UPDATE $table SET $set $join $where"
        return output.replace(FIX_SPACES_REGEX, " ").trimEnd() + ";"
    }

    private fun <T> createColumnsString(statement: Statement<T>): String {
        return statement.columns.joinToString(", ") { it.name }
    }

    private fun <T> createJoinString(statement: Statement<T>): String {
        return statement.joins.joinToString(" ") {
            "${
                it.type.name.replace(
                    '_',
                    ' '
                )
            } ${it.from.name} ON ${it.joinColumns.leftColumns[0].column.name} = ${it.joinColumns.rightColumns[0].column.name}"
        }
    }

    private fun createWhereString(whereClause: WhereClause): String {
        val output =
            """
            ${whereClause.name} 
            ${
                whereClause.parts.joinToString(" ")
                {
                    "${it.operator?.name ?: ""} ${it.output}"
                }
            }
            """.trimIndent().replace('\n', ' ')
        return output
    }


    private fun <T> createOrderByString(selectStatement: SelectStatement<T>): String {
        return selectStatement.orderBy?.parts?.joinToString(", ") {
            "${it.output} ${it.operator?.name ?: ""}"
        } ?: ""
    }
}