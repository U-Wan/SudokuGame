package com.tsu.sudokugame.controler

import android.content.Context

import com.tsu.sudokugame.controler.Symbol.Companion.getValue

import android.content.SharedPreferences
import com.tsu.sudokugame.model.data.DatabaseHelper
import com.tsu.sudokugame.model.game.GameType
import com.tsu.sudokugame.model.game.GameDifficulty
import android.content.Intent
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NewLevelManager private constructor(
    var context: Context,
    private val settings: SharedPreferences
) {
    private val dbHelper: DatabaseHelper
    private val CHALLENGE_GENERATION_PROBABILITY = 0.25
    private val CHALLENGE_ITERATIONS = 4
    fun isLevelLoadable(type: GameType?, diff: GameDifficulty?): Boolean {
        return dbHelper.getLevels(diff, type).size > 0
    }

    @Deprecated("")
    fun isLevelLoadableOld(type: GameType, diff: GameDifficulty): Boolean {
        for (file in DIR.listFiles()) {
            if (file.isFile) {
                val name = file.name.substring(0, file.name.lastIndexOf("_"))
                val sb = StringBuilder()
                sb.append(LEVEL_PREFIX)
                sb.append(type.name)
                sb.append("_")
                sb.append(diff.name)
                if (name == sb.toString()) {
                    return true
                }
            }
        }
        return false
    }

    fun loadDailySudoku(): IntArray {
        // create a seed from the current date
        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        val toHash = "Sudoku/.PrivacyFriendly/." + dateFormat.format(Date())
        val controller = QQWingController()

        // generate new sudoku using the previously computed seed
        return controller.generateFromSeed(
            toHash.hashCode(),
            CHALLENGE_GENERATION_PROBABILITY,
            CHALLENGE_ITERATIONS
        )
    }

    fun loadLevel(type: GameType?, diff: GameDifficulty?): IntArray {
        val level = dbHelper.getLevel(diff, type)
        dbHelper.deleteLevel(level.id)
        return level.puzzle
    }

    @Deprecated("")
    fun loadLevelOld(type: GameType, diff: GameDifficulty): IntArray? {
        val result: MutableList<IntArray> = LinkedList()
        val availableFiles = LinkedList<Int>()
        val r = Random()

        // go through every file
        for (file in DIR.listFiles()) {
            if (file.isFile) {
                val name = file.name.substring(0, file.name.lastIndexOf("_"))
                val number =
                    file.name.substring(file.name.lastIndexOf("_") + 1, file.name.lastIndexOf("."))
                val sb = StringBuilder()
                sb.append(LEVEL_PREFIX)
                sb.append(type.name)
                sb.append("_")
                sb.append(diff.name)
                if (name == sb.toString()) {

                    // load file
                    val bytes = ByteArray(file.length().toInt())
                    try {
                        val stream = FileInputStream(file)
                        try {
                            stream.read(bytes)
                        } finally {
                            stream.close()
                        }
                    } catch (e: IOException) {
                        Log.e("File Manager", "Could not load game. IOException occured.")
                    }
                    val gameString = String(bytes)
                    val puzzle = IntArray(type.size * type.size)
                    require(puzzle.size == gameString.length) { "Saved level is does not have the correct size." }
                    for (i in 0 until gameString.length) {
                        puzzle[i] = getValue(Symbol.SaveFormat, gameString[i].toString()) + 1
                    }
                    availableFiles.add(Integer.valueOf(number))
                    result.add(puzzle)
                }
            }
        }
        if (result.size > 0) {
            val i = r.nextInt(availableFiles.size)
            val chosen = availableFiles[i]
            val resultPuzzle = result[i]
            val sb = StringBuilder()
            sb.append(LEVEL_PREFIX)
            sb.append(type.name)
            sb.append("_")
            sb.append(diff.name)
            sb.append("_")
            sb.append(chosen)
            sb.append(FILE_EXTENSION)
            val filename = sb.toString()

            // select and delete the file
            val file = File(DIR, filename)
            file.delete()

            // then return the puzzle to load it
            return resultPuzzle
        }
        return null
    }

    fun checkAndRestock() {
        // Start Generation Service
        val i = Intent(context, GeneratorService::class.java)
        i.action = GeneratorService.ACTION_GENERATE
        //i.putExtra(ProtocolService.EXTRA_PROTOCOL, current.componentName().flattenToString());
        context.startService(i)
    }

    @Deprecated("")
    fun loadFirstStartLevelsOld() {
        // Default_9x9
        // Default_12x12
        // Default_6x6

        // Easy
        // Moderate
        // Hard

        // 0
        // 1
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Moderate,
            0,
            "000208090027000000000400000090100706408090201000030080200001000100300469000000007"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Moderate,
            1,
            "000000052000003007500206830002040700070000046640508003000400000000005000050301008"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Moderate,
            2,
            "950710600063200500100300200078000032016000000000000050000001869029000000800003000"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Moderate,
            3,
            "679000300000000050000700020020300001000006000167240000030020004004000500001003792"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Moderate,
            4,
            "000465000000000008000320500060000709900006053000043200600000902024070600000030005"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Hard,
            0,
            "000800400008004093009003060000700000000400000060002900091000670200000100403006802"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Hard,
            1,
            "000006040000050000600040080043000200500810000100300605209080460004500000001030000"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Hard,
            2,
            "080000000040000000000071300050130700004006000003508014000000081012600405070000002"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Hard,
            3,
            "030200000008000096700600050013720009006000700802009000000903082500010070000000000"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Hard,
            4,
            "007500200120093700004007050000000305500800090040300018000000009000715000000400030"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Challenge,
            0,
            "086000000000000070090000304968027030000100060200900000072006100000000296000000740"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Challenge,
            1,
            "450900001001400000600010004006700000500000000100024060002000030000062400040005700"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Challenge,
            2,
            "100000020006020091000600000030070000260040000008010000000380060700000049040100002"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Challenge,
            3,
            "040100800059408061700000002500000009080700000007004000000000090801009200000000685"
        )
        saveToFile(
            GameType.Default_9x9,
            GameDifficulty.Challenge,
            4,
            "007000050300710000140000000000500406001000907000370005790030001060004000005620000"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Moderate,
            0,
            "B30050A100701600070030800002894000007008000000B550100004020300B0000090000060000A010000000032050C0407008006A000000000400001000C290000000008005000"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Moderate,
            1,
            "00B4008A09C002A030C00008007850003000030C000408AB000B00052000000000000070069500030C00B00010467000008000000A100000000800000C0020700001700000095400"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Moderate,
            2,
            "90B08A00300100A000007B500054000B90000320000C05B0A00C0000090200050200000000000094C2000006000200303140000008006C0800000000003000C0010050007009AC60"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Moderate,
            3,
            "74000010000B000005400000900000000000000090000005000B075A0C240208B00007000009000C000207020B000860800300020190C000A000604800470009B00000A068003200"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Moderate,
            4,
            "00B000003050000AC06000700000030001800500000040000008A50402000A00100070000000069B200009130A0000000070020195C000A409000300003600000B21B00030000800"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Hard,
            0,
            "00600500000004000000002050004C00800A28049000050000A900C000000000B00000A00B0560000C900C708A00000B0002000000769000008000B002B0C6000017C00107400908"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Hard,
            1,
            "020000B000A608070530000B9050200A03800030980010B001000B6C30900000032000CAB0000000000000067000B000000500000A000C09000081302B0100070950836000100000"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Hard,
            2,
            "0A00070050B107B0060A8000005090020C0A500040001060000000A000040B00219000000107B000602C8090C000001700400A00058B00000000000620000B400090090400800000"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Hard,
            3,
            "000000908A0002900080006B0000C000007005800B2C00006020300000043B090806500C090400C000300060A700040500A00400002800000CA0000900000060B000A0B0003970C6"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Hard,
            4,
            "0070000000000020009B0100000A030000000C039002004800904005000B100B008070507040C20005A0000870B32000C5000810070980100000000000000A070C000A0500000090"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Challenge,
            0,
            "2000000000000B070C00000000AC100000000020050CA00BB60002097800079001000C60400000B0070A0000A7000298005048000010004000070009A10600C00B000C00B0000020"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Challenge,
            1,
            "01000002000C00640A800070000A0000082020000008100B0C081090000050A0060C079009BC000A04010500097000000000000000000000904000866070B100020000000C00B009"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Challenge,
            2,
            "000C0B020600400900600A020800140000C000000013050000040600900300000C0500B49A0250000000B0C0002000300143C000B0060000800064000000000B10000000020A8B09"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Challenge,
            3,
            "08060000000100000765A0000C00400000B0605001C7000019C0A4000002A000000B0040000000000100B500000080740020100005690A000000048B706000009020000020807000"
        )
        saveToFile(
            GameType.Default_12x12,
            GameDifficulty.Challenge,
            4,
            "B000730000000000090040000C00B00000003A07180000C00000000CB0000000400A200040B2000000A8000020006700710A06000C00000BA0200170A700603002B0043000090060"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Moderate,
            0,
            "000000050004013000004003006050040010"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Moderate,
            1,
            "000450000600104306630000060005010000"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Moderate,
            2,
            "020003040006000000406000000050060042"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Moderate,
            3,
            "024000510042002054000020000060600000"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Moderate,
            4,
            "610030400001004000000200060000002600"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Hard,
            0,
            "630010002000001000040020400006003040"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Hard,
            1,
            "000000060130006000050603030005000041"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Hard,
            2,
            "502360000040430050000030650000000000"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Hard,
            3,
            "000004300050006010001003000561000000"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Hard,
            4,
            "000000042000003140064030000320200000"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Challenge,
            0,
            "004200200000003002600050300400046000"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Challenge,
            1,
            "003050200003502000000000640002000045"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Challenge,
            2,
            "004030000000003020205004400000006500"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Challenge,
            3,
            "001650000320100000000060000000065000"
        )
        saveToFile(
            GameType.Default_6x6,
            GameDifficulty.Challenge,
            4,
            "500004013005004020000000042500100000"
        )
    }

    @Deprecated("")
    private fun saveToFile(
        gameType: GameType,
        gameDifficulty: GameDifficulty,
        saveNumber: Int,
        puzzle: String
    ) {
        val sb = StringBuilder()
        sb.append(LEVEL_PREFIX)
        sb.append(gameType.name)
        sb.append("_")
        sb.append(gameDifficulty.name)
        sb.append("_")
        sb.append(saveNumber)
        sb.append(FILE_EXTENSION)
        // create the file
        val file = File(DIR, sb.toString())

        // save the file
        try {
            val stream = FileOutputStream(file)
            try {
                stream.write(puzzle.toByteArray())
            } finally {
                stream.close()
            }
        } catch (e: IOException) {
            Log.e("File Manager", "Could not save game. IOException occured.")
        }
    }

    companion object {
        private var instance: NewLevelManager? = null
        private const val FILE_EXTENSION = ".txt"
        private const val LEVEL_PREFIX = "level_"
        private const val LEVELS_DIR = "level"
        private lateinit var DIR: File
        @JvmField
        var PRE_SAVES_MIN = 3
        var PRE_SAVES_MAX = 10
        @JvmStatic
        fun getInstance(context: Context, settings: SharedPreferences): NewLevelManager? {
            if (instance == null) {
                instance = NewLevelManager(context, settings)
            }
            return instance
        }
    }

    init {
        dbHelper = DatabaseHelper(context)
        DIR = context.getDir(LEVELS_DIR, 0)
    }
}