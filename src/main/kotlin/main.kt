import kotlin.browser.document

/**
 * Fix password version 1.1 from 2020-07-18.
 *
 * Char. type requirements:
 * Small letters min: 2
 * Big letters min: 2
 * Numbers min: 2
 * Special chars min: 2
 * Uses special characters: @#:!-+*?;.,
 *
 * Char base: ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#:!-+*?;.,
 */
@JsName("fixPasswordV1_1")
fun fixPasswordV1_1(str: String): String {
    return fixPassword(str, "@#:!-+*?;.,", intArrayOf(2, 2, 2, 2))
}

@JsName("fixPassword")
fun fixPassword(str: String, specialChars: String, charTypeRequirements : IntArray): String {
    val debug = false

    // define required characters frequency in the password
    val minPassLen = charTypeRequirements.sum()

    require(str.length >= minPassLen) { "Min. length of input must be ${minPassLen}. Input: $str" }

    if (debug) document.writeln("<br>To fix: $str")

    var password = str.toMutableList()

    // char type ranges
    val charTypeRanges = mutableListOf<List<Char>>()
    charTypeRanges.add(('a'..'z').toList())
    charTypeRanges.add(('A'..'Z').toList())
    charTypeRanges.add(('0'..'9').toList())
    charTypeRanges.add(specialChars.toList())

    // calculate replacing character
    val getReplacingChar: (Char, Int) -> Char = { chToReplace: Char, typeToReplaceWith: Int ->
        val i = chToReplace.toInt() % charTypeRanges[typeToReplaceWith].size
        charTypeRanges[typeToReplaceWith][i]
    }

    // analyze the password string
    val charTypeFrequency = IntArray(charTypeRequirements.size)
    //val charTypeMap = intArrayOf(str.length)
    val iTypeToReplace = mutableListOf<MutableList<Int>>()
    for (r in charTypeRanges) iTypeToReplace.add(ArrayList())
    for (i in str.indices) {
        var type = 3
        when (str[i]) {
            in 'a'..'z' -> {
                type = 0
            }
            in 'A'..'Z' -> {
                type = 1
            }
            in '0'..'9' -> {
                type = 2
            }
            else -> {
                // replace not allowed special characters
                if (!specialChars.contains(str[i])) {
                    password[i] = getReplacingChar(str[i], type)
                }
            }
        }
        charTypeFrequency[type]++
        if (charTypeFrequency[type] > charTypeRequirements[type]) iTypeToReplace[type].add(i)
    }

    if (debug) {
        document.writeln("<br>password replaced invalid special chars: ${password}")
        document.writeln("<br>iToReplace: ${iTypeToReplace.size}")
    }

    // determine what character types have to be added
    val charTypeToAdd = mutableListOf<Int>()
    for (e in charTypeFrequency.withIndex()) {
        if (debug) document.writeln("<br>${e.index} -> ${e.value}")
        for (c in 1..(charTypeRequirements[e.index] - e.value)) charTypeToAdd.add(e.index)
    }
    if (debug) document.writeln("<br>ToAdd: ${charTypeToAdd.size}")

    // uniformly distribute indexes which can be replaced according to char type
    val iToReplace = mutableListOf<Int>()
    while (true) {
        var c = 0
        for (list in iTypeToReplace) {
            if (list.size > 0) {
                iToReplace.add(list.removeAt(list.size - 1))
            } else {
                c++
            }
        }
        if (c == charTypeRanges.size) break
    }

    require(charTypeToAdd.size <= iToReplace.size) { "Characters to be added ${charTypeToAdd.size} >  ${iTypeToReplace.size} characters to be replaced." }

    // add required characters by replacing the others which can are above the required amount
    for (typeToAdd in charTypeToAdd) {
        // determine character to be replaced
        val iToReplace = iToReplace.removeAt(iToReplace.size - 1)
        val chToReplace = str[iToReplace]

        // calculate replacing character
        password[iToReplace] = getReplacingChar(chToReplace, typeToAdd)
    }

    return String(password.toCharArray())
}

fun main() {
    /*val toEncode = "ljasdlsajdsaHHHH"
    document.write("${fixPassword20200718(toEncode)}")*/
    fixPasswordV1_1("           ") // must be called otherwise the defined functions are not accessible from webpage javascript
}