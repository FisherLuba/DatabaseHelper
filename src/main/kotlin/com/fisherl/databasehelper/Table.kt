package com.fisherl.databasehelper

import com.fisherl.databasehelper.column.Column

class Table<T>(
    val name: String,
    val columns: List<Column<*>>,
    val temporary: Boolean = false,
    val unlogged: Boolean = false,
    val ifNotExists: Boolean = false
) {
}
