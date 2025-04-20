package com.bgsoftware.wildloaders.config.section;

import com.bgsoftware.wildloaders.api.config.SettingsManager;
import com.bgsoftware.wildloaders.config.SettingsContainerHolder;

import java.util.List;

public class GlobalSection extends SettingsContainerHolder implements SettingsManager.Global {

    @Override
    public List<String> getHologramLines() {
        return getContainer().hologramLines;
    }

    @Override
    public List<String> getInfiniteHologramLines() {
        return getContainer().infiniteHologramLines;
    }
}
