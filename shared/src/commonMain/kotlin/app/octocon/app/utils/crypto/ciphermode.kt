package app.octocon.app.utils.crypto

/**
 * Symmetric Cipher Mode
 */
interface CipherMode {
  companion object {
    val ECB: CipherMode get() = CipherModeECB
  }

  val name: String
  fun encrypt(data: ByteArray, cipher: Cipher, padding: Padding, iv: ByteArray?): ByteArray
  fun decrypt(data: ByteArray, cipher: Cipher, padding: Padding, iv: ByteArray?): ByteArray
}

private fun Int.nextMultipleOf(multiple: Int) =
  if (this % multiple == 0) this else (((this / multiple) + 1) * multiple)

fun CipherMode.encryptSafe(
  data: ByteArray,
  cipher: Cipher,
  padding: Padding,
  iv: ByteArray?
): ByteArray {
  if (padding == CipherPadding.NoPadding) {
    return encrypt(data, cipher, CipherPadding.ZeroPadding, iv).copyOf(data.size)
  }
  return encrypt(data, cipher, padding, iv)
}

fun CipherMode.decryptSafe(
  data: ByteArray,
  cipher: Cipher,
  padding: Padding,
  iv: ByteArray?
): ByteArray {
  if (padding == CipherPadding.NoPadding) {
    return decrypt(
      data.copyOf(data.size.nextMultipleOf(cipher.blockSize)),
      cipher,
      CipherPadding.ZeroPadding,
      iv
    ).copyOf(data.size)
  }
  return decrypt(data, cipher, padding, iv)
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

private object CipherModeECB : CipherModeBase("ECB") {
  override fun encrypt(
    data: ByteArray,
    cipher: Cipher,
    padding: Padding,
    iv: ByteArray?
  ): ByteArray {
    val pData = padding.add(data, cipher.blockSize)
    cipher.encrypt(pData, 0, pData.size)
    return pData
  }

  override fun decrypt(
    data: ByteArray,
    cipher: Cipher,
    padding: Padding,
    iv: ByteArray?
  ): ByteArray {
    cipher.decrypt(data, 0, data.size)
    return padding.remove(data)
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

private abstract class CipherModeBase(override val name: String) : CipherMode {
  override fun toString(): String = name
}

private abstract class CipherModeIV(name: String) : CipherModeBase(name) {
  final override fun encrypt(
    data: ByteArray,
    cipher: Cipher,
    padding: Padding,
    iv: ByteArray?
  ): ByteArray {
    val ivb = getIV(iv, cipher.blockSize)
    val pData = padding.add(data, cipher.blockSize)
    coreEncrypt(pData, cipher, ivb)
    return pData
  }

  final override fun decrypt(
    data: ByteArray,
    cipher: Cipher,
    padding: Padding,
    iv: ByteArray?
  ): ByteArray {
    val ivb = getIV(iv, cipher.blockSize)
    val pData = data.copyOf()
    coreDecrypt(pData, cipher, ivb)
    return padding.remove(pData)
  }

  protected abstract fun coreEncrypt(pData: ByteArray, cipher: Cipher, ivb: ByteArray)
  protected abstract fun coreDecrypt(pData: ByteArray, cipher: Cipher, ivb: ByteArray)
}

private fun getIV(srcIV: ByteArray?, blockSize: Int): ByteArray {
  if (srcIV == null) TODO("IV not provided")
  if (srcIV.size < blockSize) throw IllegalArgumentException("Wrong IV length: must be $blockSize bytes long")
  return srcIV.copyOf(blockSize)
  //return ByteArray(blockSize).also { dstIV -> arraycopy(srcIV, 0, dstIV, 0, kotlin.math.min(srcIV.size, dstIV.size)) }
}