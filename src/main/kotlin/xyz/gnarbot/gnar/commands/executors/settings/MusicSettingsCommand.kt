package xyz.gnarbot.gnar.commands.executors.settings

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Channel
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.VoiceChannel
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.Executor
import xyz.gnarbot.gnar.utils.Context

@Command(
        id = 55,
        aliases = arrayOf("music", "musicSettings", "musicconfig"),
        description = "Change music settings.",
        category = Category.SETTINGS,
        permissions = arrayOf(Permission.MANAGE_SERVER)
)
class MusicSettingsCommand : CommandTemplate() {
    @Executor(0, description = "Toggle music announcement.")
    fun toggle_announcements(context: Context) {
        if (context.guildOptions.isAnnounce) {
            context.guildOptions.isAnnounce = false
            context.guildOptions.save()

            context.send().embed("Music Settings") {
                desc {
                    "Announcements for music disabled."
                }
            }.action().queue()
        } else {
            context.guildOptions.isAnnounce = true
            context.guildOptions.save()

            context.send().embed("Music Settings") {
                desc {
                    "Announcements for music enabled."
                }
            }.action().queue()
        }
    }

    @Executor(1, description = "Add voice channels that Gnar can play music in.")
    fun music_channel_add(context: Context, channel: VoiceChannel) {
        if (channel.id in context.guildOptions.musicChannels) {
            context.send().error("`${channel.name}` is already a music channel.").queue()
            return
        }

        if (channel == context.guild.afkChannel) {
            context.send().error("`${channel.name}` is the AFK channel, you can't play music there.").queue()
            return
        }

        context.guildOptions.musicChannels.add(channel.id)
        context.guildOptions.save()

        context.send().embed("Music Settings") {
            desc {
                "`${channel.name}` is now a designated music channel."
            }
        }.action().queue()
    }

    @Executor(2, description = "Remove voice channels that Gnar can play music in.")
    fun music_channel_remove(context: Context, channel: VoiceChannel) {
        if (channel.id !in context.guildOptions.musicChannels) {
            context.send().error("`${channel.name}` is not one of the music channels.").queue()
            return
        }

        context.guildOptions.musicChannels.remove(channel.id)
        context.guildOptions.save()

        context.send().embed("Music Settings") {
            desc {
                "${channel.name} is no longer a designated music channel."
            }
        }.action().queue()
    }

    @Executor(3, description = "Set the DJ-role.")
    fun dj_set(context: Context, role: Role) {
        if (role == context.guild.publicRole) {
            context.send().error("You can't set the public role as the DJ role!").queue()
            return
        }

        if (!context.guild.selfMember.canInteract(role)) {
            context.send().error("That role is higher than my role! Fix by changing the role hierarchy.").queue()
            return
        }

        if (role.id == context.guildOptions.djRole) {
            context.send().error("${role.asMention} is already set as the DJ-role.").queue()
            return
        }

        context.guildOptions.djRole = role.id
        context.guildOptions.save()

        context.send().embed("Music Settings") {
            desc {
                "Only users with the role ${role.asMention} can now use music commands."
            }
        }.action().queue()
    }

    @Executor(4, description = "Unset the DJ-role.")
    fun dj_unset(context: Context) {
        if (context.guildOptions.djRole == null) {
            context.send().error("This guild doesn't have an DJ-role.").queue()
            return
        }

        context.guildOptions.djRole = null
        context.guildOptions.save()

        context.send().embed("Music Settings") {
            desc {
                "Unset DJ role. Everyone can now use music commands."
            }
        }.action().queue()
    }


    @Executor(5, description = "List all settings, their description and their values.")
    fun list(context: Context) {
        context.send().embed("Music Settings") {
            field("Channel") {
                buildString {
                    append("If this is not empty, Gnar will only play music in these voice channels.\n\n")
                    context.guildOptions.musicChannels.let {
                        if (it.isEmpty()) {
                            append("None.")
                        }

                        it.map(context.guild::getVoiceChannelById)
                                .filterNotNull()
                                .map(Channel::getName)
                                .forEach { append("• ").append(it).append('\n') }
                    }
                }
            }
            field("DJ Role") {
                buildString {
                    append("If this role is set, anyone with this role will bypass music permission requirements.\n\n")
                    append(context.guildOptions.djRole?.let { context.guild.getRoleById(it) }?.asMention ?: "None")
                }
            }
        }.action().queue()
    }
}