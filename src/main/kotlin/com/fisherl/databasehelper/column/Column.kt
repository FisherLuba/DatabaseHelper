package com.fisherl.databasehelper.column

import com.fisherl.databasehelper.Collation

class Column<T> private constructor(
    val type: Type<T>,
    private val tableName: String,
    val shortName: String,
    val collation: Collation?,
    val storageType: StorageType?,
    val compressionType: CompressionType?,
    val constraints: Array<out Constraint>
) {

    val name = "${tableName}.$shortName"

    fun format(value: T): String {
        return type.format(value)
    }

    enum class StorageType {

        PLAIN,
        EXTERNAL,
        EXTENDED,
        MAIN,
        DEFAULT

    }

    enum class CompressionType {

        PGLZ,
        LZ4,
    }

    enum class Constraint {

        NOT_NULL,
        UNIQUE,
        PRIMARY_KEY

    }

    abstract class Type<T>(val name: String) {

        companion object {

            fun varchar(size: Int): Type<String> {
                return object : Type<String>("VARCHAR($size)") {
                    override fun format(value: String): String {
                        return "'$value'"
                    }
                }
            }

        }

        abstract fun format(value: T): String

        data object INT : Type<Int>("INT") {
            override fun format(value: Int): String {
                return value.toString()
            }
        }
        data object TEXT : Type<String>("TEXT") {
            override fun format(value: String): String {
                return "'$value'"
            }
        }
        data object BOOLEAN : Type<Boolean>("BOOLEAN") {
            override fun format(value: Boolean): String {
                return value.toString()
            }
        }
        data object TIMESTAMP : Type<Long>("TIMESTAMP") {
            override fun format(value: Long): String {
                return value.toString()
            }
        }
        data object JSON : Type<String>("JSON") {
            override fun format(value: String): String {
                return value
            }
        }
        data object BLOB : Type<ByteArray>("BLOB") {
            override fun format(value: ByteArray): String {
                return value.toString()
            }
        }
        data object UUID : Type<UUID>("UUID") {
            override fun format(value: UUID): String {
                return value.toString()
            }
        }

        fun createColumn(
            tableName: String,
            name: String,
            collation: Collation? = null,
            storageType: StorageType? = null,
            compressionType: CompressionType? = null,
            vararg constraints: Constraint = emptyArray()
        ): Column<T> {
            return Column(
                this,
                tableName,
                name,
                collation,
                storageType,
                compressionType,
                constraints
            )
        }

    }

}