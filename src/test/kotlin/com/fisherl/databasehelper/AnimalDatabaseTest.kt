package com.fisherl.databasehelper

import com.fisherl.databasehelper.dialect.PostgresDialect
import com.fisherl.databasehelper.field.Column
import com.fisherl.databasehelper.query.ClauseInput
import com.fisherl.databasehelper.query.DeleteStatement
import com.fisherl.databasehelper.query.InsertStatement
import com.fisherl.databasehelper.query.JoinColumn
import com.fisherl.databasehelper.query.JoinColumns
import com.fisherl.databasehelper.query.OrderByClause
import com.fisherl.databasehelper.query.SelectStatement
import com.fisherl.databasehelper.query.TableJoin
import com.fisherl.databasehelper.query.UpdateStatement
import com.fisherl.databasehelper.query.WhereClause
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

const val ANIMAL_TABLE_NAME = "animals"
const val MAMMALS_TABLE_NAME = "mammals"

sealed class Animal(
    val id: Int,
    val name: String,
    val description: String
)

class Mammal(
    id: Int,
    name: String,
    description: String
) : Animal(id, name, description)

private val ANIMAL_ID_COL = Column.Type.INT.createColumn(
    ANIMAL_TABLE_NAME,
    "id",
    constraints = arrayOf(
        Column.Constraint.NOT_NULL,
        Column.Constraint.PRIMARY_KEY
    )
)
private val ANIMAL_NAME_COL = Column.Type.varchar(255).createColumn(
    ANIMAL_TABLE_NAME,
    "name",
    constraints = arrayOf(Column.Constraint.NOT_NULL)
)
private val ANIMAL_DESCRIPTION_COL = Column.Type.TEXT.createColumn(
    ANIMAL_TABLE_NAME,
    "description",
    constraints = arrayOf(Column.Constraint.NOT_NULL)
)

val ANIMAL_TABLE = Table<Animal>(
    ANIMAL_TABLE_NAME,
    listOf(ANIMAL_ID_COL, ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
    temporary = false,
    unlogged = false,
    ifNotExists = true
)

private val MAMMAL_ID_COL = Column.Type.INT.createColumn(
    MAMMALS_TABLE_NAME,
    "id",
    constraints = arrayOf(
        Column.Constraint.NOT_NULL,
        Column.Constraint.PRIMARY_KEY
    )
)

private val MAMMAL_NAME_COL = Column.Type.varchar(255).createColumn(
    MAMMALS_TABLE_NAME,
    "name",
    constraints = arrayOf(Column.Constraint.NOT_NULL)
)

private val MAMMAL_DESCRIPTION_COL = Column.Type.TEXT.createColumn(
    MAMMALS_TABLE_NAME,
    "description",
    constraints = arrayOf(Column.Constraint.NOT_NULL)
)

val MAMMALS_TABLE = Table<Mammal>(
    MAMMALS_TABLE_NAME,
    listOf(MAMMAL_ID_COL, MAMMAL_NAME_COL, MAMMAL_DESCRIPTION_COL),
    temporary = false,
    unlogged = false,
    ifNotExists = true
)

val SELECT_ALL_ANIMALS = SelectStatement.Builder(
    ANIMAL_TABLE,
    listOf(ANIMAL_ID_COL, ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
    emptyList(),
    null,
    null,
    null,
    null
).build()


val SELECT_ALL_ANIMALS_WHERE_ID_EQ_1_AND_NAME_NEQ_TEST = SelectStatement.Builder(
    ANIMAL_TABLE,
    listOf(ANIMAL_ID_COL, ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
    emptyList(),
    WhereClause.builder().equal(ClauseInput.column(ANIMAL_ID_COL), 1)
        .and().notEqual(ClauseInput.column(ANIMAL_NAME_COL), "test"),
    null,
    null,
    null
).build()

val SELECT_ALL_ANIMALS_WHERE_ID_EQ_1_AND_NAME_NEQ_TEST_AND_ORDER_BY_ID_ASC =
    SelectStatement.Builder(
        ANIMAL_TABLE,
        listOf(ANIMAL_ID_COL, ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
        emptyList(),
        WhereClause.builder().equal(ClauseInput.column(ANIMAL_ID_COL), 1)
            .and().notEqual(ClauseInput.column(ANIMAL_NAME_COL), "test"),
        OrderByClause.builder().asc().column(ANIMAL_ID_COL),
        null,
        null
    ).build()

val SELECT_ALL_ANIMALS_JOINED_WITH_MAMMALS = SelectStatement.Builder(
    ANIMAL_TABLE,
    listOf(ANIMAL_ID_COL, ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
    listOf(
        TableJoin(
            MAMMALS_TABLE,
            TableJoin.Type.INNER_JOIN,
            JoinColumns.Builder().add(
                JoinColumn(ANIMAL_TABLE, ANIMAL_ID_COL),
                JoinColumn(MAMMALS_TABLE, MAMMAL_ID_COL),
                TableJoin.Type.INNER_JOIN
            ).build()
        )
    ),
    null,
    null,
    null,
    null
).build()

val INSERT_ONE_ANIMAL = InsertStatement(
    ANIMAL_TABLE,
    listOf(ANIMAL_ID_COL, ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
    emptyList()
)

val INSERT_ONE_ANIMAL_AND_SELECT_ALL = InsertStatement(
    ANIMAL_TABLE,
    listOf(ANIMAL_ID_COL, ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
    emptyList(),
    SELECT_ALL_ANIMALS
)

val DELETE_ANIMALS_WHERE_ID_EQ_1 = DeleteStatement(
    ANIMAL_TABLE,
    emptyList(),
    WhereClause.builder().equal(ClauseInput.column(ANIMAL_ID_COL), 1),
)

val UPDATE_ANIMALS_WHERE_ID_EQ_1 = UpdateStatement(
    ANIMAL_TABLE,
    listOf(ANIMAL_NAME_COL, ANIMAL_DESCRIPTION_COL),
    emptyList(),
    WhereClause.builder().equal(ClauseInput.column(ANIMAL_ID_COL), 1),
)

class AnimalDatabaseTest {

    @Test
    fun testTable() {
        val statement = PostgresDialect.createTableStatement(ANIMAL_TABLE)
        assertEquals(
            "CREATE TABLE IF NOT EXISTS animals (id INT NOT NULL PRIMARY KEY, name VARCHAR(255) NOT NULL, description TEXT NOT NULL);",
            statement
        )
    }

    @Test
    fun testSelectStatement() {
        val statement = PostgresDialect.createSelectStatement(
            SELECT_ALL_ANIMALS_WHERE_ID_EQ_1_AND_NAME_NEQ_TEST
        )
        assertEquals(
            "SELECT animals.id, animals.name, animals.description FROM animals WHERE animals.id = 1 AND animals.name != 'test';",
            statement
        )
    }

    @Test
    fun testSelectStatementWithOrderBy() {
        val statement = PostgresDialect.createSelectStatement(
            SELECT_ALL_ANIMALS_WHERE_ID_EQ_1_AND_NAME_NEQ_TEST_AND_ORDER_BY_ID_ASC
        )
        assertEquals(
            "SELECT animals.id, animals.name, animals.description FROM animals WHERE animals.id = 1 AND animals.name != 'test' ORDER BY animals.id ASC;",
            statement
        )
    }

    @Test
    fun testSelectStatementWithJoin() {
        val statement =
            PostgresDialect.createSelectStatement(SELECT_ALL_ANIMALS_JOINED_WITH_MAMMALS)
        assertEquals(
            "SELECT animals.id, animals.name, animals.description FROM animals INNER JOIN mammals ON animals.id = mammals.id;",
            statement
        )
    }

    @Test
    fun testInsertStatement() {
        val statement = PostgresDialect.createInsertStatement(INSERT_ONE_ANIMAL)
        assertEquals(
            "INSERT INTO animals (animals.id, animals.name, animals.description) VALUES (?, ?, ?);",
            statement
        )
    }

    @Test
    fun testInsertStatementWithSelect() {
        val statement = PostgresDialect.createInsertStatement(INSERT_ONE_ANIMAL_AND_SELECT_ALL)
        assertEquals(
            "INSERT INTO animals (animals.id, animals.name, animals.description) VALUES (?, ?, ?) SELECT animals.id, animals.name, animals.description FROM animals;",
            statement
        )
    }

    @Test
    fun testDeleteStatement() {
        val statement = PostgresDialect.createDeleteStatement(DELETE_ANIMALS_WHERE_ID_EQ_1)
        assertEquals(
            "DELETE FROM animals WHERE animals.id = 1;",
            statement
        )
    }

    @Test
    fun testUpdateStatement() {
        val statement = PostgresDialect.createUpdateStatement(UPDATE_ANIMALS_WHERE_ID_EQ_1)
        assertEquals(
            "UPDATE animals SET animals.name = ?, animals.description = ? WHERE animals.id = 1;",
            statement
        )
    }

}