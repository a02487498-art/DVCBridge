package fortniop.dvcbridge;

import static fortniop.dvcbridge.Core.platform;

public class FabricVoicechatPlugin extends VoicechatPlugin {
    @Override
    protected void ensurePlatformInitialized() {
        if (platform == null) {
            platform = new FabricPlatform();
        }
    }
}
