package fortniop.dvcbridge;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.*;

import static fortniop.dvcbridge.Constants.PLUGIN_ID;
import static fortniop.dvcbridge.Core.api;
import static fortniop.dvcbridge.Core.platform;

public abstract class VoicechatPlugin implements de.maxhenkel.voicechat.api.VoicechatPlugin {
    public VoicechatPlugin() {
        ensurePlatformInitialized();
    }

    protected abstract void ensurePlatformInitialized();

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi serverApi) {
        api = (VoicechatServerApi) serverApi;
        platform.info("Successfully initialized Simple Voice Chat plugin");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(JoinGroupEvent.class, GroupManager::onJoinGroup);
        registration.registerEvent(LeaveGroupEvent.class, GroupManager::onLeaveGroup);
        registration.registerEvent(CreateGroupEvent.class, GroupManager::onGroupCreated);
        registration.registerEvent(RemoveGroupEvent.class, GroupManager::onGroupRemoved);
    }
}
