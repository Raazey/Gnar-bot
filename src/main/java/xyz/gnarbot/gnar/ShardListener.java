package xyz.gnarbot.gnar;

import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

class ShardListener extends ListenerAdapter
{
    private final Shard shard;
    
    public ShardListener(Shard shard)
    {
        this.shard = shard;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        shard.getHost(event.getGuild()).handleMessageEvent(event);
    }
}