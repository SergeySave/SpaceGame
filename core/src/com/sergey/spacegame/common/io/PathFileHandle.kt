package com.sergey.spacegame.common.io

import com.badlogic.gdx.Files
import com.badlogic.gdx.files.FileHandle
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Path

typealias NFiles = java.nio.file.Files

/**
 * @author sergeys
 *
 * This does not support file navigation. It is a read-only FileHandle wrapper for a java nio path.
 *
 * The list methods will return empty arrays.
 * Any write methods will throw unsupported operation exceptions unless they can safely fail (ie. return false).
 *
 *
 */
class PathFileHandle(val name: String, val path: Path) : FileHandle() {
    
    val simpleName by lazy { name.substringBeforeLast('.') }
    val pathStringName by lazy { path.toString().substringBeforeLast('.') }
    val extension by lazy { name.substringAfterLast('.') }
    
    override fun list(): Array<FileHandle> = arrayOf()
    override fun list(filter: FileFilter?): Array<FileHandle> = arrayOf()
    override fun list(filter: FilenameFilter?): Array<FileHandle> = arrayOf()
    override fun list(suffix: String?): Array<FileHandle> = arrayOf()
    
    override fun writeString(string: String?,
                             append: Boolean) = unsupOp("PathFileHandle does not support write methods")
    
    override fun writeString(string: String?, append: Boolean,
                             charset: String?) = unsupOp("PathFileHandle does not support write methods")
    
    override fun deleteDirectory(): Boolean = false
    
    override fun writeBytes(bytes: ByteArray?,
                            append: Boolean) = unsupOp("PathFileHandle does not support write methods")
    
    override fun writeBytes(bytes: ByteArray?, offset: Int, length: Int,
                            append: Boolean) = unsupOp("PathFileHandle does not support write methods")
    
    override fun extension(): String = extension
    
    override fun pathWithoutExtension(): String = simpleName
    
    override fun write(append: Boolean): OutputStream = unsupOp("PathFileHandle does not support write methods")
    override fun write(append: Boolean,
                       bufferSize: Int): OutputStream = unsupOp("PathFileHandle does not support write methods")
    
    override fun write(input: InputStream?, append: Boolean) = unsupOp("PathFileHandle does not support write methods")
    
    override fun file(): File = path.toFile()
    
    override fun moveTo(dest: FileHandle?) = unsupOp("PathFileHandle does not support write methods")
    override fun mkdirs() = unsupOp("PathFileHandle does not support write methods")
    
    override fun sibling(name: String?): FileHandle? = null
    
    override fun type(): Files.FileType = Files.FileType.Absolute
    
    override fun readString(): String = NFiles.readAllLines(path).joinToString("\n")
    override fun readString(
            charset: String?): String = NFiles.readAllLines(path, Charset.forName(charset)).joinToString("\n")
    
    override fun lastModified(): Long = NFiles.getLastModifiedTime(path).toMillis()
    
    override fun read(): InputStream = NFiles.newInputStream(path)
    override fun read(bufferSize: Int): BufferedInputStream = BufferedInputStream(read(), bufferSize)
    override fun readBytes(): ByteArray = NFiles.readAllBytes(path)
    override fun readBytes(bytes: ByteArray, offset: Int, size: Int): Int {
        val byteChannel = NFiles.newByteChannel(path)
        byteChannel.position()
        val byteBuffer = ByteBuffer.allocate(size)
        byteChannel.read(byteBuffer)
        var position = 0
        while (byteBuffer.hasRemaining()) {
            bytes[position++] = byteBuffer.get()
        }
        return position
    }
    
    override fun delete(): Boolean = unsupOp("PathFileHandle does not support write methods")
    override fun parent(): FileHandle = unsupOp("PathFileHandle does not support parent()")
    
    override fun hashCode(): Int = path.hashCode()
    
    override fun path(): String = path.toString()
    
    override fun length(): Long = NFiles.size(path)
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathFileHandle) return false
        if (!super.equals(other)) return false
        
        if (name != other.name) return false
        if (path != other.path) return false
        
        return true
    }
    
    override fun copyTo(dest: FileHandle?) = unsupOp("PathFileHandle does not support write methods")
    
    override fun writer(append: Boolean): Writer = unsupOp("PathFileHandle does not support write methods")
    override fun writer(append: Boolean,
                        charset: String?): Writer = unsupOp("PathFileHandle does not support write methods")
    
    override fun toString(): String {
        return "PathFileHandle(path=$path)"
    }
    
    override fun nameWithoutExtension(): String = pathStringName
    
    override fun name(): String = path.toString()
    
    override fun isDirectory(): Boolean = NFiles.isDirectory(path)
    
    override fun reader(): Reader = NFiles.newBufferedReader(path)
    override fun reader(charset: String?): Reader = NFiles.newBufferedReader(path, Charset.forName(charset))
    override fun reader(bufferSize: Int): BufferedReader = BufferedReader(NFiles.newBufferedReader(path), bufferSize)
    override fun reader(bufferSize: Int,
                        charset: String?): BufferedReader = BufferedReader(NFiles.newBufferedReader(path, Charset.forName(charset)), bufferSize)
    
    override fun exists(): Boolean = NFiles.exists(path)
    
    override fun child(name: String?): FileHandle? = null
    
    override fun emptyDirectory() = unsupOp("PathFileHandle does not support write methods")
    
    override fun emptyDirectory(preserveTree: Boolean) = unsupOp("PathFileHandle does not support write methods")
    
    private fun unsupOp(msg: String): Nothing = throw UnsupportedOperationException(msg)
}