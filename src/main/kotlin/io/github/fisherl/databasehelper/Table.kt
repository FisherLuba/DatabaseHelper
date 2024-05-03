package io.github.fisherl.databasehelper

import io.github.fisherl.databasehelper.field.Column

class Table(
    val name: String,
    val columns: List<Column<*>>,
    val temporary: Boolean = false,
    val unlogged: Boolean = false,
    val ifNotExists: Boolean = false
) {
}
