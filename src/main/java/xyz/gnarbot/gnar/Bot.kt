package xyz.gnarbot.gnar

import net.dv8tion.jda.JDABuilder
import net.dv8tion.jda.entities.User
import net.dv8tion.jda.utils.SimpleLog
import xyz.gnarbot.gnar.utils.readProperties
import java.io.File
import java.util.Date
import java.util.concurrent.Executors
import kotlin.jvm.JvmStatic as static

fun main(args : Array<String>)
{
    Bot.initBot(Bot.authTokens.getProperty("main-bot"), 4)
}

/**
 * Main class of the bot. Implemented as a singleton.
 */
object Bot
{
    @static val LOG = SimpleLog.getLog("Bot")!!
    
    var initialized = false
        private set
    
    val shards = mutableListOf<Shard>()
    val admins = mutableSetOf<User>()
    
    val startTime = System.currentTimeMillis()
    val authTokens = File("_DATA/tokens.properties").readProperties()
    val scheduler = Executors.newSingleThreadScheduledExecutor()
    
    fun initBot(token : String, num_shards : Int)
    {
        if (initialized) throw IllegalStateException("Bot instance have already been initialized.")
        initialized = true
        
        LOG.info("Initializing Bot.")
        LOG.info("Bot token is \"$token\".")
        LOG.info("Requesting $num_shards shards.")
        
        val adminIDs = File("_DATA/administrators").readLines()
        
        for (id in 0 .. num_shards - 1)
        {
            val jda = JDABuilder().run {
                if (num_shards > 1) useSharding(id, num_shards)
                setBotToken(token)
                setAutoReconnect(true)
                buildBlocking()
            }
            
            jda.accountManager.setUsername("GNAR")
            jda.accountManager.setGame("_help | _invite")
            jda.accountManager.update()
            
            adminIDs.map { jda.getUserById(it) }.forEach { admins += it }
            
            shards += Shard(id, jda)
        }
        
        LOG.info("Bot is now connected to discord.")
    }
    
    val uptime : String
        get()
        {
            val s = (Date().time - startTime) / 1000
            val m = s / 60
            val h = m / 60
            val d = h / 24
            return "$d days, ${h % 24} hours, ${m % 60} minutes and ${s % 60} seconds"
        }
    
    val simpleUptime : String
        get()
        {
            val s = (Date().time - startTime) / 1000
            val m = s / 60
            val h = m / 60
            val d = h / 24
            return "${d}d ${h % 24}h ${m % 60}m ${s % 60}s"
        }
}