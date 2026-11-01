package io.github.thevoidblock.headbrowser;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;

@Modmenu(modId = MOD_ID)
@Config(name = MOD_ID, wrapperName = "HeadBrowserConfig")
@SuppressWarnings("unused")
public class HeadBrowserConfigModel {
    public boolean modEnabled = true;
    public boolean tooltipHints = true;
    public boolean autoQuery = false;
    public boolean pauseButton = true;
    public boolean titleButton = true;

    @SectionHeader("advanced")
    public long cacheExpirationTime = 86400;
}
