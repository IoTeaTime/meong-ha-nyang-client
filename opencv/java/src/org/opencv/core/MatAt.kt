package org.opencv.core

import org.opencv.core.Mat.Tuple3

fun Mat.get(row: Int, col: Int, data: UByteArray): Int {
    val byteArray = data.toByteArray() // UByteArray -> ByteArray
    val result = this.get(row, col, byteArray)
    byteArray.copyInto(data.asByteArray()) // 변환된 데이터를 다시 UByteArray로 복사
    return result
}

fun Mat.get(indices: IntArray, data: UByteArray): Int {
    val byteArray = data.toByteArray() // UByteArray -> ByteArray
    val result = this.get(indices, byteArray)
    byteArray.copyInto(data.asByteArray()) // 변환된 데이터를 다시 UByteArray로 복사
    return result
}

fun Mat.put(row: Int, col: Int, data: UByteArray): Int {
    val byteArray = data.toByteArray() // UByteArray -> ByteArray
    return this.put(row, col, byteArray)
}

fun Mat.put(indices: IntArray, data: UByteArray): Int {
    val byteArray = data.toByteArray() // UByteArray -> ByteArray
    return this.put(indices, byteArray)
}

fun Mat.get(row: Int, col: Int, data: UShortArray): Int {
    val shortArray = data.toShortArray() // UShortArray -> ShortArray
    val result = this.get(row, col, shortArray)
    shortArray.copyInto(data.asShortArray()) // 변환된 데이터를 다시 UShortArray로 복사
    return result
}

fun Mat.get(indices: IntArray, data: UShortArray): Int {
    val shortArray = data.toShortArray() // UShortArray -> ShortArray
    val result = this.get(indices, shortArray)
    shortArray.copyInto(data.asShortArray()) // 변환된 데이터를 다시 UShortArray로 복사
    return result
}

fun Mat.put(row: Int, col: Int, data: UShortArray): Int {
    val shortArray = data.toShortArray() // UShortArray -> ShortArray
    return this.put(row, col, shortArray)
}

fun Mat.put(indices: IntArray, data: UShortArray): Int {
    val shortArray = data.toShortArray() // UShortArray -> ShortArray
    return this.put(indices, shortArray)
}

private fun UByteArray.toByteArray(): ByteArray = this.map { it.toByte() }.toByteArray()
private fun UShortArray.toShortArray(): ShortArray = this.map { it.toShort() }.toShortArray()
private fun ByteArray.copyInto(ubyteArray: UByteArray) {
    this.forEachIndexed { index, value -> ubyteArray[index] = value.toUByte() }
}
private fun ShortArray.copyInto(ushortArray: UShortArray) {
    this.forEachIndexed { index, value -> ushortArray[index] = value.toUShort() }
}
