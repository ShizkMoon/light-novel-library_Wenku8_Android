package org.mewx.wenku8.reader.view

import org.mewx.wenku8.reader.loader.WenkuReaderLoader

class WenkuReaderPaginator(
    private val loader: WenkuReaderLoader,
    private val textMeasurer: TextMeasurer,
    private val textAreaWidth: Int,
    private val textAreaHeight: Int,
    private val fontHeight: Int,
    private val pxLineDistance: Int,
    private val pxParagraphDistance: Int,
) {
    private var firstLineIndex = 0
    private var firstWordIndex = 0
    private var lastLineIndex = 0
    private var lastWordIndex = 0
    private val lineInfoList = mutableListOf<LineInfo>()

    fun setPageStart(lineIndex: Int, wordIndex: Int) {
        firstLineIndex = lineIndex
        firstWordIndex = wordIndex
        loader.setCurrentIndex(firstLineIndex)
    }

    fun setPageEnd(lineIndex: Int, wordIndex: Int) {
        lastLineIndex = lineIndex
        lastWordIndex = wordIndex
        loader.setCurrentIndex(lastLineIndex)
    }

    /**
     * Calc page from first to last.
     * firstLineIndex & firstWordIndex set.
     */
    fun calcFromFirst() {
        var widthSum = 0
        var heightSum = fontHeight
        var tempText = StringBuilder()
        lineInfoList.clear()

        var curLineIndex = firstLineIndex
        var curWordIndex = firstWordIndex
        while (curLineIndex < loader.getElementCount()) {
            if (curWordIndex == 0 && loader.getCurrentType() == WenkuReaderLoader.ElementType.TEXT) {
                widthSum = 2 * fontHeight
                tempText = StringBuilder("　　")
            } else if (loader.getCurrentType() == WenkuReaderLoader.ElementType.IMAGE_DEPENDENT) {
                if (lineInfoList.isNotEmpty()) {
                    lastLineIndex = loader.getCurrentIndex() - 1
                    loader.setCurrentIndex(lastLineIndex)
                    lastWordIndex = loader.getCurrentStringLength() - 1
                    break
                }

                lastLineIndex = loader.getCurrentIndex()
                firstLineIndex = lastLineIndex
                firstWordIndex = 0
                lastWordIndex = loader.getCurrentStringLength() - 1
                lineInfoList.add(
                    LineInfo(
                        WenkuReaderLoader.ElementType.IMAGE_DEPENDENT,
                        requireNotNull(loader.getCurrentAsString()),
                    ),
                )
                break
            }

            val currentString = loader.getCurrentAsString()
            if (currentString == null || loader.getCurrentStringLength() == 0) {
                curWordIndex = 0
                if (curLineIndex >= loader.getElementCount()) {
                    break
                }
                curLineIndex += 1
                loader.setCurrentIndex(curLineIndex)
                continue
            }

            val temp = currentString[curWordIndex].toString()
            val tempWidth = textMeasurer.measureText(temp).toInt()

            if (widthSum + tempWidth > textAreaWidth) {
                lineInfoList.add(LineInfo(WenkuReaderLoader.ElementType.TEXT, tempText.toString()))
                heightSum += pxLineDistance

                if (heightSum + fontHeight > textAreaHeight) {
                    if (curWordIndex > 0) {
                        lastLineIndex = curLineIndex
                        lastWordIndex = curWordIndex - 1
                    } else if (curLineIndex > 0) {
                        curLineIndex -= 1
                        loader.setCurrentIndex(curLineIndex)
                        lastLineIndex = curLineIndex
                        lastWordIndex = loader.getCurrentStringLength() - 1
                    } else {
                        lastLineIndex = 0
                        lastWordIndex = 0
                    }
                    break
                }

                tempText = StringBuilder(temp)
                widthSum = tempWidth
                heightSum += fontHeight
            } else {
                tempText.append(temp)
                widthSum += tempWidth
            }

            if (curWordIndex + 1 >= loader.getCurrentStringLength()) {
                lineInfoList.add(LineInfo(WenkuReaderLoader.ElementType.TEXT, tempText.toString()))
                heightSum += pxParagraphDistance

                if (heightSum + fontHeight > textAreaHeight) {
                    lastLineIndex = loader.getCurrentIndex()
                    lastWordIndex = loader.getCurrentStringLength() - 1
                    break
                }

                heightSum += fontHeight
                widthSum = 0
                tempText = StringBuilder()
                curWordIndex = 0
                if (curLineIndex + 1 >= loader.getElementCount()) {
                    lastLineIndex = curLineIndex
                    lastWordIndex = loader.getCurrentStringLength() - 1
                    break
                }
                curLineIndex += 1
                loader.setCurrentIndex(curLineIndex)
            } else {
                curWordIndex += 1
            }
        }
    }

    /**
     * Calc page from last to first.
     * lastLineIndex & lastWordIndex set.
     */
    fun calcFromLast() {
        var heightSum = 0
        var isFirst = true
        loader.setCurrentIndex(lastLineIndex)
        lineInfoList.clear()

        var curLineIndex = lastLineIndex
        var curWordIndex = lastWordIndex
        lineLoop@ while (curLineIndex >= 0) {
            val curType = loader.getCurrentType()
            val curString = loader.getCurrentAsString()

            if (curString.isNullOrEmpty()) {
                if (curLineIndex - 1 >= 0) {
                    curLineIndex -= 1
                    loader.setCurrentIndex(curLineIndex)
                    curWordIndex = loader.getCurrentStringLength()
                } else {
                    firstLineIndex = 0
                    firstWordIndex = 0
                    loader.setCurrentIndex(firstLineIndex)
                    lineInfoList.clear()
                    calcFromFirst()
                    break
                }
                continue
            }

            if (curType == WenkuReaderLoader.ElementType.IMAGE_DEPENDENT && lineInfoList.isNotEmpty()) {
                firstLineIndex = curLineIndex + 1
                firstWordIndex = 0
                loader.setCurrentIndex(firstLineIndex)
                lineInfoList.clear()
                calcFromFirst()
                break
            } else if (curType == WenkuReaderLoader.ElementType.IMAGE_DEPENDENT) {
                lastLineIndex = loader.getCurrentIndex()
                firstLineIndex = lastLineIndex
                firstWordIndex = 0
                lastWordIndex = loader.getCurrentStringLength() - 1
                lineInfoList.add(
                    LineInfo(
                        WenkuReaderLoader.ElementType.IMAGE_DEPENDENT,
                        requireNotNull(loader.getCurrentAsString()),
                    ),
                )
                break
            }

            var tempWidth = 0
            val curList = mutableListOf<LineInfo>()
            var temp = ""
            var i = 0
            while (i < curString.length) {
                if (i == 0) {
                    tempWidth += fontHeight + fontHeight
                    temp = "　　"
                }

                val char = curString[i].toString()
                val width = textMeasurer.measureText(char).toInt()
                if (tempWidth + width > textAreaWidth) {
                    curList.add(LineInfo(WenkuReaderLoader.ElementType.TEXT, temp))

                    if (i >= curWordIndex) {
                        break
                    }

                    tempWidth = 0
                    temp = ""
                    continue
                } else {
                    temp += char
                    tempWidth += width
                    i += 1
                }

                if (i == curString.length) {
                    curList.add(LineInfo(WenkuReaderLoader.ElementType.TEXT, temp))
                }
            }

            for (index in curList.size - 1 downTo 0) {
                if (isFirst) {
                    isFirst = false
                } else if (index == curList.size - 1) {
                    heightSum += pxParagraphDistance
                } else {
                    heightSum += pxLineDistance
                }

                heightSum += fontHeight
                if (heightSum > textAreaHeight) {
                    var indexCount = -2
                    for (j in 0..index) {
                        indexCount += curList[j].text().length
                    }
                    firstLineIndex = curLineIndex
                    firstWordIndex = indexCount + 1

                    if (firstWordIndex + 1 >= curString.length) {
                        firstLineIndex = curLineIndex + 1
                        firstWordIndex = 0
                    }
                    break@lineLoop
                }
                lineInfoList.add(0, curList[index])
            }

            if (curLineIndex - 1 >= 0) {
                curLineIndex -= 1
                loader.setCurrentIndex(curLineIndex)
                curWordIndex = loader.getCurrentStringLength()
            } else {
                firstLineIndex = 0
                firstWordIndex = 0
                loader.setCurrentIndex(firstLineIndex)
                lineInfoList.clear()
                calcFromFirst()
                break
            }
        }
    }

    fun getLineInfoList(): List<LineInfo> = lineInfoList

    fun getFirstLineIndex(): Int = firstLineIndex

    fun getFirstWordIndex(): Int = firstWordIndex

    fun getLastLineIndex(): Int = lastLineIndex

    fun getLastWordIndex(): Int = lastWordIndex
}
